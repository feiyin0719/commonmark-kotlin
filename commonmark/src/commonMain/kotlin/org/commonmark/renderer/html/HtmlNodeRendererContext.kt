package org.commonmark.renderer.html

import org.commonmark.node.Node

/**
 * The context for rendering HTML, passed to [HtmlNodeRendererFactory.create] and used by node renderers.
 */
public interface HtmlNodeRendererContext {

    /**
     * @param url to be encoded
     * @return an encoded URL (depending on the configuration)
     */
    public fun encodeUrl(url: String): String

    /**
     * Let extensions modify the HTML tag attributes.
     *
     * @param node       the node for which the attributes are applied
     * @param tagName    the HTML tag name that these attributes are for (e.g. `h1`, `pre`, `code`).
     * @param attributes the attributes that were calculated by the renderer
     * @return the extended attributes with added/updated/removed entries
     */
    public fun extendAttributes(node: Node, tagName: String, attributes: Map<String, String?>): Map<String, String?>

    /**
     * @return the HTML writer to use
     */
    public fun getWriter(): HtmlWriter

    /**
     * @return HTML that should be rendered for a soft line break
     */
    public fun getSoftbreak(): String

    /**
     * Render the specified node and its children using the configured renderers. This should be used to render child
     * nodes; be careful not to pass the node that is being rendered, that would result in an endless loop.
     *
     * @param node the node to render
     */
    public fun render(node: Node)

    /**
     * @return whether HTML blocks and tags should be escaped or not
     */
    public fun shouldEscapeHtml(): Boolean

    /**
     * @return whether documents that only contain a single paragraph should be rendered without the `<p>` tag
     */
    public fun shouldOmitSingleParagraphP(): Boolean

    /**
     * @return true if the [UrlSanitizer] should be used.
     */
    public fun shouldSanitizeUrls(): Boolean

    /**
     * @return Sanitizer to use for securing link href and image src if [shouldSanitizeUrls] is true.
     */
    public fun urlSanitizer(): UrlSanitizer
}
