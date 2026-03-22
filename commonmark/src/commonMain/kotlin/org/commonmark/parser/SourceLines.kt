package org.commonmark.parser

import org.commonmark.node.SourceSpan

/**
 * A set of lines ([SourceLine]) from the input source.
 */
public class SourceLines private constructor() {
    private val _lines = mutableListOf<SourceLine>()
    public val lines: MutableList<SourceLine> get() = _lines

    public fun addLine(sourceLine: SourceLine) {
        _lines.add(sourceLine)
    }

    public fun isEmpty(): Boolean = _lines.isEmpty()

    public fun getContent(): String {
        val sb = StringBuilder()
        for (i in _lines.indices) {
            if (i != 0) {
                sb.append('\n')
            }
            sb.append(_lines[i].content)
        }
        return sb.toString()
    }

    public fun getSourceSpans(): List<SourceSpan> {
        val sourceSpans = mutableListOf<SourceSpan>()
        for (line in _lines) {
            val sourceSpan = line.sourceSpan
            if (sourceSpan != null) {
                sourceSpans.add(sourceSpan)
            }
        }
        return sourceSpans
    }

    public companion object {
        public fun empty(): SourceLines = SourceLines()

        public fun of(sourceLine: SourceLine): SourceLines {
            val sourceLines = SourceLines()
            sourceLines.addLine(sourceLine)
            return sourceLines
        }

        public fun of(lines: List<SourceLine>): SourceLines {
            val result = SourceLines()
            result._lines.addAll(lines)
            return result
        }
    }
}
