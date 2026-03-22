package org.commonmark.node

public class OrderedList : ListBlock() {
    public var markerDelimiter: String? = null
    public var markerStartNumber: Int? = null

    override fun accept(visitor: Visitor): Unit = visitor.visit(this)

    override fun toStringAttributes(): String =
        "markerDelimiter=$markerDelimiter, markerStartNumber=$markerStartNumber"
}
