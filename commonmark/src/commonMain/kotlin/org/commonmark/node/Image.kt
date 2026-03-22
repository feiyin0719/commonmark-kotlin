package org.commonmark.node

public class Image : Node {
    public var destination: String
    public var title: String?

    public constructor(destination: String = "", title: String? = null) {
        this.destination = destination
        this.title = title
    }

    override fun accept(visitor: Visitor): Unit = visitor.visit(this)

    override fun toStringAttributes(): String = "destination=$destination, title=$title"
}
