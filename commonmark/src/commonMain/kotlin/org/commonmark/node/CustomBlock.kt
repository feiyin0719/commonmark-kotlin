package org.commonmark.node

public abstract class CustomBlock : Block() {
    override fun accept(visitor: Visitor): Unit = visitor.visit(this)
}
