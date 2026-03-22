package org.commonmark.node

public class LinkReferenceDefinition(
    public var label: String? = null,
    public var destination: String? = null,
    public var title: String? = null
) : Block() {
    override fun accept(visitor: Visitor): Unit = visitor.visit(this)

    override fun toStringAttributes(): String =
        "label=$label, destination=$destination, title=$title"
}
