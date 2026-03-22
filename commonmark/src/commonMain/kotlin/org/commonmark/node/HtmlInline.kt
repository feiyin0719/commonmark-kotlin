package org.commonmark.node

public class HtmlInline : Node() {
    public var literal: String? = null

    override fun accept(visitor: Visitor): Unit = visitor.visit(this)

    override fun toStringAttributes(): String = "literal=$literal"
}
