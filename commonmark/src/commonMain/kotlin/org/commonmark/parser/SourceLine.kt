package org.commonmark.parser

import org.commonmark.node.SourceSpan

/**
 * A line or part of a line from the input source.
 */
public class SourceLine private constructor(
    public val content: CharSequence,
    public val sourceSpan: SourceSpan?
) {
    public fun substring(beginIndex: Int, endIndex: Int): SourceLine {
        val newContent = content.subSequence(beginIndex, endIndex)
        var newSourceSpan: SourceSpan? = null
        if (sourceSpan != null) {
            val length = endIndex - beginIndex
            if (length != 0) {
                val columnIndex = sourceSpan.columnIndex + beginIndex
                val inputIndex = sourceSpan.inputIndex + beginIndex
                newSourceSpan = SourceSpan.of(sourceSpan.lineIndex, columnIndex, inputIndex, length)
            }
        }
        return of(newContent, newSourceSpan)
    }

    public companion object {
        public fun of(content: CharSequence, sourceSpan: SourceSpan?): SourceLine {
            return SourceLine(content, sourceSpan)
        }
    }
}
