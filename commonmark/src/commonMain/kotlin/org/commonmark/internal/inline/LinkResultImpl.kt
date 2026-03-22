package org.commonmark.internal.inline

import org.commonmark.node.Node
import org.commonmark.parser.beta.LinkResult
import org.commonmark.parser.beta.Position

internal class LinkResultImpl(
    val type: Type,
    val node: Node,
    val position: Position,
) : LinkResult {
    var markerIncluded: Boolean = false
        private set

    override fun includeMarker(): LinkResult {
        markerIncluded = true
        return this
    }

    enum class Type {
        WRAP,
        REPLACE,
    }
}
