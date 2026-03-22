package org.commonmark.node

public class Document : Block() {
    override fun accept(visitor: Visitor): Unit = visitor.visit(this)
}
