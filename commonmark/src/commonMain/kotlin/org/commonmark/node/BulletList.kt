package org.commonmark.node

public class BulletList : ListBlock() {
    public var marker: String? = null

    override fun accept(visitor: Visitor): Unit = visitor.visit(this)

    override fun toStringAttributes(): String = "marker=$marker"
}
