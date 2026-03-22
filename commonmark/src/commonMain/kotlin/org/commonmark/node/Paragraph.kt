package org.commonmark.node

public class Paragraph : Block() {
    override fun accept(visitor: Visitor): Unit = visitor.visit(this)
}
