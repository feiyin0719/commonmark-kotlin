package org.commonmark.node

public object Nodes {
    public fun between(
        start: Node,
        end: Node,
    ): Iterable<Node> {
        return Iterable {
            object : Iterator<Node> {
                var node: Node? = start.next

                override fun hasNext(): Boolean = node != null && node !== end

                override fun next(): Node {
                    val current = node ?: throw NoSuchElementException()
                    node = current.next
                    return current
                }
            }
        }
    }
}
