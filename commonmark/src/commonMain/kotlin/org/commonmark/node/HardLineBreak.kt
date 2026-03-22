package org.commonmark.node

public class HardLineBreak : Node() {
    override fun accept(visitor: Visitor): Unit = visitor.visit(this)
}
