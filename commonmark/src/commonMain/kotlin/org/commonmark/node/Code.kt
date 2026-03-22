package org.commonmark.node

public class Code : Node {
    public var literal: String

    public constructor(literal: String = "") {
        this.literal = literal
    }

    override fun accept(visitor: Visitor): Unit = visitor.visit(this)

    override fun toStringAttributes(): String = "literal=$literal"
}
