package org.commonmark.node

/**
 * A utility class for aggregating source spans from multiple nodes.
 */
public class SourceSpans private constructor() {
    private val spans = mutableListOf<SourceSpan>()

    public fun getSourceSpans(): List<SourceSpan> = spans.toList()

    public fun addAllFrom(nodes: Iterable<Node>) {
        for (node in nodes) {
            addAll(node.getSourceSpans())
        }
    }

    public fun addAll(other: List<SourceSpan>) {
        if (other.isEmpty()) return
        if (spans.isNotEmpty()) {
            val last = spans.last()
            val first = other.first()
            if (last.lineIndex == first.lineIndex &&
                last.inputIndex + last.length == first.inputIndex
            ) {
                spans[spans.size - 1] = SourceSpan.of(
                    last.lineIndex, last.columnIndex, last.inputIndex,
                    last.length + first.length
                )
                for (i in 1 until other.size) {
                    spans.add(other[i])
                }
                return
            }
        }
        spans.addAll(other)
    }

    public companion object {
        public fun empty(): SourceSpans = SourceSpans()
    }
}
