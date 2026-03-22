package org.commonmark.renderer.text

import org.commonmark.renderer.NodeRenderer

/**
 * Factory for instantiating new node renderers when rendering is done.
 */
public fun interface TextContentNodeRendererFactory {

    /**
     * Create a new node renderer for the specified rendering context.
     *
     * @param context the context for rendering (normally passed on to the node renderer)
     * @return a node renderer
     */
    public fun create(context: TextContentNodeRendererContext): NodeRenderer
}
