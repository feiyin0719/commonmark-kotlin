package org.commonmark.test

import org.commonmark.node.AbstractVisitor
import org.commonmark.node.Node

/**
 * Renders source spans in a document by inserting bracket markers around the spans in the original source text.
 * Useful for testing that source span tracking is working correctly.
 */
object SourceSpanRenderer {
    /**
     * Render source spans in the document using source position's line and column index.
     */
    fun renderWithLineColumn(
        document: Node,
        source: String,
    ): String {
        val visitor = SourceSpanMarkersVisitor()
        document.accept(visitor)
        val lineColumnMarkers = visitor.lineColumnMarkers

        val sb = StringBuilder()
        val lines =
            source.split("\n").let {
                // Match Java's String.split behavior: drop trailing empty strings
                var end = it.size
                while (end > 0 && it[end - 1].isEmpty()) end--
                it.subList(0, end)
            }

        for (lineIndex in lines.indices) {
            val line = lines[lineIndex]
            val lineMarkers = lineColumnMarkers[lineIndex]
            for (i in line.indices) {
                appendMarkers(lineMarkers, i, sb)
                sb.append(line[i])
            }
            appendMarkers(lineMarkers, line.length, sb)
            sb.append("\n")
        }

        return sb.toString()
    }

    /**
     * Render source spans in the document using source position's input index.
     */
    fun renderWithInputIndex(
        document: Node,
        source: String,
    ): String {
        val visitor = SourceSpanMarkersVisitor()
        document.accept(visitor)
        val markers = visitor.inputIndexMarkers

        val sb = StringBuilder()
        for (i in source.indices) {
            markers[i]?.forEach { marker -> sb.append(marker) }
            sb.append(source[i])
        }
        return sb.toString()
    }

    private fun appendMarkers(
        lineMarkers: MutableMap<Int, MutableList<String>>?,
        columnIndex: Int,
        sb: StringBuilder,
    ) {
        if (lineMarkers != null) {
            val columnMarkers = lineMarkers[columnIndex]
            if (columnMarkers != null) {
                for (marker in columnMarkers) {
                    sb.append(marker)
                }
            }
        }
    }

    private class SourceSpanMarkersVisitor : AbstractVisitor() {
        val lineColumnMarkers = mutableMapOf<Int, MutableMap<Int, MutableList<String>>>()
        val inputIndexMarkers = mutableMapOf<Int, MutableList<String>>()

        private var markerIndex = 0

        override fun visitChildren(parent: Node) {
            val sourceSpans = parent.getSourceSpans()
            if (sourceSpans.isNotEmpty()) {
                for (span in sourceSpans) {
                    val opener = OPENING[markerIndex % OPENING.length].toString()
                    val closer = CLOSING[markerIndex % CLOSING.length].toString()

                    val line = span.lineIndex
                    val col = span.columnIndex
                    val input = span.inputIndex
                    val length = span.length

                    getMarkers(line, col).add(opener)
                    getMarkers(line, col + length).add(0, closer)

                    inputIndexMarkers.getOrPut(input) { mutableListOf() }.add(opener)
                    inputIndexMarkers.getOrPut(input + length) { mutableListOf() }.add(0, closer)
                }
                markerIndex++
            }
            super.visitChildren(parent)
        }

        private fun getMarkers(
            lineIndex: Int,
            columnIndex: Int,
        ): MutableList<String> {
            val columnMap = lineColumnMarkers.getOrPut(lineIndex) { mutableMapOf() }
            return columnMap.getOrPut(columnIndex) { mutableListOf() }
        }

        companion object {
            private const val OPENING = "({[<\u2E22\u2E24"
            private const val CLOSING = ")}]>\u2E23\u2E25"
        }
    }
}
