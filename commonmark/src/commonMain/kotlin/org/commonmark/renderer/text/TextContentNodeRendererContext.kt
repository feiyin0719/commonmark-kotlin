package org.commonmark.renderer.text

import org.commonmark.node.Node

/**
 * The context for text content rendering, passed to [TextContentNodeRendererFactory.create] and used by node renderers.
 */
public interface TextContentNodeRendererContext {

    /**
     * Controls how line breaks should be rendered, see [LineBreakRendering].
     */
    public fun lineBreakRendering(): LineBreakRendering

    /**
     * @return true for stripping new lines and render text as "single line",
     * false for keeping all line breaks.
     */
    @Deprecated("Use lineBreakRendering() instead")
    public fun stripNewlines(): Boolean

    /**
     * @return the writer to use
     */
    public fun getWriter(): TextContentWriter

    /**
     * Render the specified node and its children using the configured renderers. This should be used to render child
     * nodes; be careful not to pass the node that is being rendered, that would result in an endless loop.
     *
     * @param node the node to render
     */
    public fun render(node: Node)
}
