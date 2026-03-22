package org.commonmark.parser.beta

import org.commonmark.internal.inline.LinkResultImpl
import org.commonmark.node.Node

/**
 * What to do with a link/image processed by [LinkProcessor].
 */
public interface LinkResult {
    public fun includeMarker(): LinkResult

    public companion object {
        public fun none(): LinkResult? = null

        public fun wrapTextIn(
            node: Node,
            position: Position,
        ): LinkResult = LinkResultImpl(LinkResultImpl.Type.WRAP, node, position)

        public fun replaceWith(
            node: Node,
            position: Position,
        ): LinkResult = LinkResultImpl(LinkResultImpl.Type.REPLACE, node, position)
    }
}
