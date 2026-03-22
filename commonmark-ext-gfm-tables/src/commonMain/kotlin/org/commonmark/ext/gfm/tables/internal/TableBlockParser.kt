package org.commonmark.ext.gfm.tables.internal

import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableBody
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.node.Block
import org.commonmark.parser.InlineParser
import org.commonmark.parser.SourceLine
import org.commonmark.parser.SourceLines
import org.commonmark.parser.block.AbstractBlockParser
import org.commonmark.parser.block.AbstractBlockParserFactory
import org.commonmark.parser.block.BlockContinue
import org.commonmark.parser.block.BlockStart
import org.commonmark.parser.block.MatchedBlockParser
import org.commonmark.parser.block.ParserState
import org.commonmark.text.Characters

internal class TableBlockParser private constructor(
    private val columns: List<TableCellInfo>,
    headerLine: SourceLine
) : AbstractBlockParser() {

    override val block: Block = TableBlock()
    private val rowLines: MutableList<SourceLine> = mutableListOf(headerLine)

    override var canHaveLazyContinuationLines: Boolean = true
        private set

    override fun tryContinue(parserState: ParserState): BlockContinue? {
        val content = parserState.line.content
        val pipe = Characters.find('|', content, parserState.nextNonSpaceIndex)
        if (pipe != -1) {
            if (pipe == parserState.nextNonSpaceIndex) {
                // If we *only* have a pipe character (and whitespace), that is not a valid table row and ends the table.
                if (Characters.skipSpaceTab(content, pipe + 1, content.length) == content.length) {
                    // We also don't want the pipe to be added via lazy continuation.
                    canHaveLazyContinuationLines = false
                    return BlockContinue.none()
                }
            }
            return BlockContinue.atIndex(parserState.index)
        } else {
            return BlockContinue.none()
        }
    }

    override fun addLine(line: SourceLine) {
        rowLines.add(line)
    }

    override fun parseInlines(inlineParser: InlineParser) {
        val sourceSpans = block.getSourceSpans()

        val headerSourceSpan = if (sourceSpans.isNotEmpty()) sourceSpans[0] else null
        val head = TableHead()
        if (headerSourceSpan != null) {
            head.addSourceSpan(headerSourceSpan)
        }
        block.appendChild(head)

        val headerRow = TableRow()
        headerRow.setSourceSpans(head.getSourceSpans())
        head.appendChild(headerRow)

        val headerCells = split(rowLines[0])
        val headerColumns = headerCells.size
        for (i in 0 until headerColumns) {
            val cell = headerCells[i]
            val tableCell = parseCell(cell, i, inlineParser)
            tableCell.isHeader = true
            headerRow.appendChild(tableCell)
        }

        var body: TableBody? = null
        // Body starts at index 2. 0 is header, 1 is separator.
        for (rowIndex in 2 until rowLines.size) {
            val rowLine = rowLines[rowIndex]
            val sourceSpan = if (rowIndex < sourceSpans.size) sourceSpans[rowIndex] else null
            val cells = split(rowLine)
            val row = TableRow()
            if (sourceSpan != null) {
                row.addSourceSpan(sourceSpan)
            }

            // Body can not have more columns than head
            for (i in 0 until headerColumns) {
                val cell = if (i < cells.size) cells[i] else SourceLine.of("", null)
                val tableCell = parseCell(cell, i, inlineParser)
                row.appendChild(tableCell)
            }

            if (body == null) {
                // It's valid to have a table without body. In that case, don't add an empty TableBody node.
                body = TableBody()
                block.appendChild(body)
            }
            body.appendChild(row)
            if (sourceSpan != null) {
                body.addSourceSpan(sourceSpan)
            }
        }
    }

    private fun parseCell(cell: SourceLine, column: Int, inlineParser: InlineParser): TableCell {
        val tableCell = TableCell()
        val sourceSpan = cell.sourceSpan
        if (sourceSpan != null) {
            tableCell.addSourceSpan(sourceSpan)
        }

        if (column < columns.size) {
            val cellInfo = columns[column]
            tableCell.alignment = cellInfo.alignment
            tableCell.width = cellInfo.width
        }

        val content = cell.content
        val start = Characters.skipSpaceTab(content, 0, content.length)
        val end = Characters.skipSpaceTabBackwards(content, content.length - 1, start)
        inlineParser.parse(SourceLines.of(cell.substring(start, end + 1)), tableCell)

        return tableCell
    }

    class Factory : AbstractBlockParserFactory() {

        override fun tryStart(state: ParserState, matchedBlockParser: MatchedBlockParser): BlockStart? {
            val paragraphLines = matchedBlockParser.paragraphLines.lines
            if (paragraphLines.size >= 1 && Characters.find('|', paragraphLines[paragraphLines.size - 1].content, 0) != -1) {
                val line = state.line
                val separatorLine = line.substring(state.index, line.content.length)
                val columns = parseSeparator(separatorLine.content)
                if (columns != null && columns.isNotEmpty()) {
                    val paragraph = paragraphLines[paragraphLines.size - 1]
                    val headerCells = split(paragraph)
                    if (columns.size >= headerCells.size) {
                        return BlockStart.of(TableBlockParser(columns, paragraph))
                            .atIndex(state.index)
                            .replaceParagraphLines(1)
                    }
                }
            }
            return BlockStart.none()
        }
    }

    private class TableCellInfo(val alignment: TableCell.Alignment?, val width: Int)

    companion object {

        // Examples of valid separators:
        //
        // |-
        // -|
        // |-|
        // -|-
        // |-|-|
        // --- | ---
        private fun parseSeparator(s: CharSequence): List<TableCellInfo>? {
            val columns = mutableListOf<TableCellInfo>()
            var pipes = 0
            var valid = false
            var i = 0
            var width = 0
            while (i < s.length) {
                val c = s[i]
                when (c) {
                    '|' -> {
                        i++
                        pipes++
                        if (pipes > 1) {
                            // More than one adjacent pipe not allowed
                            return null
                        }
                        // Need at least one pipe, even for a one column table
                        valid = true
                    }
                    '-', ':' -> {
                        if (pipes == 0 && columns.isNotEmpty()) {
                            // Need a pipe after the first column (first column doesn't need to start with one)
                            return null
                        }
                        var left = false
                        var right = false
                        if (c == ':') {
                            left = true
                            i++
                            width++
                        }
                        var haveDash = false
                        while (i < s.length && s[i] == '-') {
                            i++
                            width++
                            haveDash = true
                        }
                        if (!haveDash) {
                            // Need at least one dash
                            return null
                        }
                        if (i < s.length && s[i] == ':') {
                            right = true
                            i++
                            width++
                        }
                        columns.add(TableCellInfo(getAlignment(left, right), width))
                        width = 0
                        // Next, need another pipe
                        pipes = 0
                    }
                    ' ', '\t' -> {
                        // White space is allowed between pipes and columns
                        i++
                    }
                    else -> {
                        // Any other character is invalid
                        return null
                    }
                }
            }
            if (!valid) {
                return null
            }
            return columns
        }

        private fun getAlignment(left: Boolean, right: Boolean): TableCell.Alignment? {
            return if (left && right) {
                TableCell.Alignment.CENTER
            } else if (left) {
                TableCell.Alignment.LEFT
            } else if (right) {
                TableCell.Alignment.RIGHT
            } else {
                null
            }
        }

        private fun split(line: SourceLine): List<SourceLine> {
            val row = line.content
            val nonSpace = Characters.skipSpaceTab(row, 0, row.length)
            var cellStart = nonSpace
            var cellEnd = row.length
            if (row[nonSpace] == '|') {
                // This row has leading/trailing pipes - skip the leading pipe
                cellStart = nonSpace + 1
                // Strip whitespace from the end but not the pipe or we could miss an empty ("||") cell
                val nonSpaceEnd = Characters.skipSpaceTabBackwards(row, row.length - 1, cellStart)
                cellEnd = nonSpaceEnd + 1
            }
            val cells = mutableListOf<SourceLine>()
            val sb = StringBuilder()
            var i = cellStart
            while (i < cellEnd) {
                val c = row[i]
                when (c) {
                    '\\' -> {
                        if (i + 1 < cellEnd && row[i + 1] == '|') {
                            // Pipe is special for table parsing. An escaped pipe doesn't result in a new cell, but is
                            // passed down to inline parsing as an unescaped pipe. Note that that applies even for the `\|`
                            // in an input like `\\|` - in other words, table parsing doesn't support escaping backslashes.
                            sb.append('|')
                            i++
                        } else {
                            // Preserve backslash before other characters or at end of line.
                            sb.append('\\')
                        }
                    }
                    '|' -> {
                        val content = sb.toString()
                        cells.add(SourceLine.of(content, line.substring(cellStart, i).sourceSpan))
                        sb.clear()
                        // + 1 to skip the pipe itself for the next cell's span
                        cellStart = i + 1
                    }
                    else -> {
                        sb.append(c)
                    }
                }
                i++
            }
            if (sb.isNotEmpty()) {
                val content = sb.toString()
                cells.add(SourceLine.of(content, line.substring(cellStart, line.content.length).sourceSpan))
            }
            return cells
        }
    }
}
