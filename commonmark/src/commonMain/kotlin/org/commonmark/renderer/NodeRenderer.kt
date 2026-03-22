package org.commonmark.renderer

import org.commonmark.node.Node
import kotlin.reflect.KClass

/**
 * A renderer for a set of node types.
 */
public interface NodeRenderer {

    /**
     * @return the types of nodes that this renderer handles
     */
    public fun getNodeTypes(): Set<KClass<out Node>>

    /**
     * Render the specified node.
     *
     * @param node the node to render, will be an instance of one of [getNodeTypes]
     */
    public fun render(node: Node)

    /**
     * Called before the root node is rendered, to do any initial processing at the start.
     *
     * @param rootNode the root (top-level) node
     */
    public fun beforeRoot(rootNode: Node) {}

    /**
     * Called after the root node is rendered, to do any final processing at the end.
     *
     * @param rootNode the root (top-level) node
     */
    public fun afterRoot(rootNode: Node) {}
}
