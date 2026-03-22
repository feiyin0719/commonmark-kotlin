package org.commonmark.internal.inline

import org.commonmark.node.Node
import org.commonmark.parser.beta.LinkResult
import org.commonmark.parser.beta.Position

internal class LinkResultImpl(
    val type: Type,
    val node: Node,
    val position: Position
) : LinkResult {
    var includeMarker: Boolean = false
        private set

    override fun includeMarker(): LinkResult {
        includeMarker = true
        return this
    }

    enum class Type {
        WRAP,
        REPLACE
    }
}
