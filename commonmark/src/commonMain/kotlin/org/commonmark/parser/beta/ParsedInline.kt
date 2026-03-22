package org.commonmark.parser.beta

import org.commonmark.internal.inline.ParsedInlineImpl
import org.commonmark.node.Node

/**
 * The result of a single inline parser.
 */
public interface ParsedInline {
    public companion object {
        public fun none(): ParsedInline? = null

        public fun of(node: Node, position: Position): ParsedInline {
            return ParsedInlineImpl(node, position)
        }
    }
}
