package org.commonmark.node

/**
 * A source span, which tracks a range in the original input.
 */
public class SourceSpan private constructor(
    public val lineIndex: Int,
    public val columnIndex: Int,
    public val inputIndex: Int,
    public val length: Int,
) {
    public fun subSpan(beginIndex: Int): SourceSpan = subSpan(beginIndex, length)

    public fun subSpan(
        beginIndex: Int,
        endIndex: Int,
    ): SourceSpan {
        require(beginIndex >= 0) { "beginIndex $beginIndex must be >= 0" }
        require(beginIndex <= length) { "beginIndex $beginIndex must be <= length $length" }
        require(endIndex >= 0) { "endIndex $endIndex must be >= 0" }
        require(endIndex <= length) { "endIndex $endIndex must be <= length $length" }
        require(beginIndex <= endIndex) { "beginIndex $beginIndex must be <= endIndex $endIndex" }
        if (beginIndex == 0 && endIndex == length) {
            return this
        }
        return of(lineIndex, columnIndex + beginIndex, inputIndex + beginIndex, endIndex - beginIndex)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is SourceSpan) return false
        return lineIndex == other.lineIndex &&
            columnIndex == other.columnIndex &&
            inputIndex == other.inputIndex &&
            length == other.length
    }

    override fun hashCode(): Int {
        var result = lineIndex
        result = 31 * result + columnIndex
        result = 31 * result + inputIndex
        result = 31 * result + length
        return result
    }

    override fun toString(): String = "SourceSpan{line=$lineIndex, col=$columnIndex, input=$inputIndex, length=$length}"

    public companion object {
        public fun of(
            lineIndex: Int,
            columnIndex: Int,
            inputIndex: Int,
            length: Int,
        ): SourceSpan = SourceSpan(lineIndex, columnIndex, inputIndex, length)
    }
}
