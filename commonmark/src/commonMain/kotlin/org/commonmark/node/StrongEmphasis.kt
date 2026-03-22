package org.commonmark.node

public class StrongEmphasis :
    Node(),
    Delimited {
    public var delimiter: String? = null

    override val openingDelimiter: String?
        get() = delimiter

    override val closingDelimiter: String?
        get() = delimiter

    override fun accept(visitor: Visitor): Unit = visitor.visit(this)
}
