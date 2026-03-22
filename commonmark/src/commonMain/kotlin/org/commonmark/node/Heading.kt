package org.commonmark.node

public class Heading : Block() {
    public var level: Int = 0

    override fun accept(visitor: Visitor): Unit = visitor.visit(this)

    override fun toStringAttributes(): String = "level=$level"
}
