package org.commonmark.parser.beta

import org.commonmark.node.SourceSpan
import org.commonmark.parser.SourceLine
import org.commonmark.parser.SourceLines
import org.commonmark.text.CharMatcher

public class Scanner internal constructor(
    private val lines: List<SourceLine>,
    private var lineIndex: Int,
    private var index: Int
) {
    private var line: SourceLine = SourceLine.of("", null)
    private var lineLength: Int = 0

    init {
        if (lines.isNotEmpty()) {
            checkPosition(lineIndex, index)
            setLine(lines[lineIndex])
        }
    }

    public fun peek(): Char {
        if (index < lineLength) {
            return line.content[index]
        } else {
            return if (lineIndex < lines.size - 1) '\n' else END
        }
    }

    public fun peekCodePoint(): Int {
        if (index < lineLength) {
            val c = line.content[index]
            if (c.isHighSurrogate() && index + 1 < lineLength) {
                val low = line.content[index + 1]
                if (low.isLowSurrogate()) {
                    return toCodePoint(c, low)
                }
            }
            return c.code
        } else {
            return if (lineIndex < lines.size - 1) '\n'.code else END.code
        }
    }

    public fun peekPreviousCodePoint(): Int {
        if (index > 0) {
            val prev = index - 1
            val c = line.content[prev]
            if (c.isLowSurrogate() && prev > 0) {
                val high = line.content[prev - 1]
                if (high.isHighSurrogate()) {
                    return toCodePoint(high, c)
                }
            }
            return c.code
        } else {
            return if (lineIndex > 0) '\n'.code else END.code
        }
    }

    public fun hasNext(): Boolean {
        if (index < lineLength) {
            return true
        }
        return lineIndex < lines.size - 1
    }

    public fun next() {
        index++
        if (index > lineLength) {
            lineIndex++
            if (lineIndex < lines.size) {
                setLine(lines[lineIndex])
            } else {
                setLine(SourceLine.of("", null))
            }
            index = 0
        }
    }

    public fun next(c: Char): Boolean {
        if (peek() == c) {
            next()
            return true
        }
        return false
    }

    public fun next(content: String): Boolean {
        if (index < lineLength && index + content.length <= lineLength) {
            for (i in content.indices) {
                if (line.content[index + i] != content[i]) {
                    return false
                }
            }
            index += content.length
            return true
        }
        return false
    }

    public fun matchMultiple(c: Char): Int {
        var count = 0
        while (peek() == c) {
            count++
            next()
        }
        return count
    }

    public fun match(matcher: CharMatcher): Int {
        var count = 0
        while (matcher.matches(peek())) {
            count++
            next()
        }
        return count
    }

    public fun whitespace(): Int {
        var count = 0
        while (true) {
            when (peek()) {
                ' ', '\t', '\n', '\u000B', '\u000C', '\r' -> {
                    count++
                    next()
                }
                else -> return count
            }
        }
    }

    public fun find(c: Char): Int {
        var count = 0
        while (true) {
            val cur = peek()
            if (cur == END) return -1
            if (cur == c) return count
            count++
            next()
        }
    }

    public fun find(matcher: CharMatcher): Int {
        var count = 0
        while (true) {
            val c = peek()
            if (c == END) return -1
            if (matcher.matches(c)) return count
            count++
            next()
        }
    }

    public fun position(): Position = Position(lineIndex, index)

    public fun setPosition(position: Position) {
        checkPosition(position.lineIndex, position.index)
        this.lineIndex = position.lineIndex
        this.index = position.index
        setLine(lines[this.lineIndex])
    }

    public fun getSource(begin: Position, end: Position): SourceLines {
        if (begin.lineIndex == end.lineIndex) {
            val sourceLine = lines[begin.lineIndex]
            val newContent = sourceLine.content.subSequence(begin.index, end.index)
            var newSourceSpan: SourceSpan? = null
            val sourceSpan = sourceLine.sourceSpan
            if (sourceSpan != null) {
                newSourceSpan = sourceSpan.subSpan(begin.index, end.index)
            }
            return SourceLines.of(SourceLine.of(newContent, newSourceSpan))
        } else {
            val sourceLines = SourceLines.empty()
            val firstLine = lines[begin.lineIndex]
            sourceLines.addLine(firstLine.substring(begin.index, firstLine.content.length))
            for (l in begin.lineIndex + 1 until end.lineIndex) {
                sourceLines.addLine(lines[l])
            }
            val lastLine = lines[end.lineIndex]
            sourceLines.addLine(lastLine.substring(0, end.index))
            return sourceLines
        }
    }

    private fun setLine(line: SourceLine) {
        this.line = line
        this.lineLength = line.content.length
    }

    private fun checkPosition(lineIndex: Int, index: Int) {
        require(lineIndex in lines.indices) {
            "Line index $lineIndex out of range, number of lines: ${lines.size}"
        }
        val line = lines[lineIndex]
        require(index in 0..line.content.length) {
            "Index $index out of range, line length: ${line.content.length}"
        }
    }

    private fun toCodePoint(high: Char, low: Char): Int {
        return ((high.code - 0xD800) shl 10) + (low.code - 0xDC00) + 0x10000
    }

    public companion object {
        public const val END: Char = '\u0000'

        public fun of(lines: SourceLines): Scanner = Scanner(lines.lines, 0, 0)
    }
}
