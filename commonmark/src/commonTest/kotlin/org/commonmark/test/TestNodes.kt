package org.commonmark.test

import org.commonmark.node.Node
import kotlin.reflect.KClass

/**
 * Test utility for finding and collecting child nodes.
 *
 * Named TestNodes to avoid conflict with [org.commonmark.node.Nodes] in the main source.
 */
object TestNodes {

    /**
     * Get all direct children of the given parent node.
     */
    fun getChildren(parent: Node): List<Node> {
        val children = mutableListOf<Node>()
        var child = parent.firstChild
        while (child != null) {
            children.add(child)
            child = child.next
        }
        return children
    }

    /**
     * Recursively try to find a node with the given type within the children of the specified node.
     *
     * @param parent The node to get children from (node itself will not be checked)
     * @param nodeClass The type of node to find
     * @return The first matching node, or null if not found
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Node> tryFind(parent: Node, nodeClass: KClass<T>): T? {
        var node = parent.firstChild
        while (node != null) {
            val next = node.next
            if (nodeClass.isInstance(node)) {
                return node as T
            }
            val result = tryFind(node, nodeClass)
            if (result != null) {
                return result
            }
            node = next
        }
        return null
    }

    /**
     * Recursively try to find a node with the given type within the children of the specified node.
     * Throws if node could not be found.
     */
    fun <T : Node> find(parent: Node, nodeClass: KClass<T>): T {
        return tryFind(parent, nodeClass)
            ?: error("Could not find a ${nodeClass.simpleName} node in $parent")
    }
}

/**
 * Inline reified version of [TestNodes.tryFind] for convenience.
 */
inline fun <reified T : Node> Node.tryFind(): T? = TestNodes.tryFind(this, T::class)

/**
 * Inline reified version of [TestNodes.find] for convenience.
 */
inline fun <reified T : Node> Node.find(): T = TestNodes.find(this, T::class)
