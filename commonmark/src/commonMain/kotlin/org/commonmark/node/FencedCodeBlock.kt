package org.commonmark.node

public class FencedCodeBlock : Block() {
    public var fenceCharacter: String? = null
    public var openingFenceLength: Int? = null
    public var closingFenceLength: Int? = null
    public var fenceIndent: Int = 0
    public var info: String? = null
    public var literal: String? = null

    override fun accept(visitor: Visitor): Unit = visitor.visit(this)
}
