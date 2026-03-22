package org.commonmark.node

public class BlockQuote : Block() {
    override fun accept(visitor: Visitor): Unit = visitor.visit(this)
}
