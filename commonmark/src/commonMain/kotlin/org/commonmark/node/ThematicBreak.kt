package org.commonmark.node

public class ThematicBreak : Block() {
    public var literal: String? = null

    override fun accept(visitor: Visitor): Unit = visitor.visit(this)
}
