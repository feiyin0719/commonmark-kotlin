package org.commonmark.parser

/**
 * Whether to include [org.commonmark.node.SourceSpan] or not while parsing.
 */
public enum class IncludeSourceSpans {
    /** Do not include source spans. */
    NONE,

    /** Include source spans on [org.commonmark.node.Block] nodes. */
    BLOCKS,

    /** Include source spans on block nodes and inline nodes. */
    BLOCKS_AND_INLINES,
}
