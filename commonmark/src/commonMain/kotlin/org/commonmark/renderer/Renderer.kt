package org.commonmark.renderer

import org.commonmark.node.Node

/**
 * Render nodes to an output / string.
 */
public interface Renderer {

    /**
     * Render the tree of nodes to output.
     *
     * @param node the root node
     * @param output output for rendering
     */
    public fun render(node: Node, output: StringBuilder)

    /**
     * Render the tree of nodes to string.
     *
     * @param node the root node
     * @return the rendered string
     */
    public fun render(node: Node): String
}
