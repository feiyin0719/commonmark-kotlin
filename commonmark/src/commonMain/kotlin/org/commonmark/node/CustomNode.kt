package org.commonmark.node

public abstract class CustomNode : Node() {
    override fun accept(visitor: Visitor): Unit = visitor.visit(this)
}
