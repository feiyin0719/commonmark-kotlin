package org.commonmark.node

public class SoftLineBreak : Node() {
    override fun accept(visitor: Visitor): Unit = visitor.visit(this)
}
