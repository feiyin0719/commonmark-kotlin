package org.commonmark.node

public class ListItem : Block() {
    public var markerIndent: Int? = null
    public var contentIndent: Int? = null

    override fun accept(visitor: Visitor): Unit = visitor.visit(this)
}
