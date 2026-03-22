package org.commonmark.renderer.html

import org.commonmark.Extension
import org.commonmark.internal.renderer.NodeRendererMap
import org.commonmark.internal.util.Escaping
import org.commonmark.node.Node
import org.commonmark.renderer.NodeRenderer
import org.commonmark.renderer.Renderer

/**
 * Renders a tree of nodes to HTML.
 *
 * Start with the [builder] method to configure the renderer. Example:
 * ```
 * val renderer = HtmlRenderer.builder().escapeHtml(true).build()
 * renderer.render(node)
 * ```
 */
public class HtmlRenderer private constructor(builder: Builder) : Renderer {

    private val softbreak: String = builder.softbreak
    private val escapeHtml: Boolean = builder.escapeHtml
    private val percentEncodeUrls: Boolean = builder.percentEncodeUrls
    private val omitSingleParagraphP: Boolean = builder.omitSingleParagraphP
    private val sanitizeUrls: Boolean = builder.sanitizeUrls
    private val urlSanitizer: UrlSanitizer = builder.urlSanitizer
    private val attributeProviderFactories: List<AttributeProviderFactory> = ArrayList(builder.attributeProviderFactories)
    private val nodeRendererFactories: List<HtmlNodeRendererFactory>

    init {
        val factories = ArrayList<HtmlNodeRendererFactory>(builder.nodeRendererFactories.size + 1)
        factories.addAll(builder.nodeRendererFactories)
        // Add as last. This means clients can override the rendering of core nodes if they want.
        factories.add(HtmlNodeRendererFactory { context -> CoreHtmlNodeRenderer(context) })
        this.nodeRendererFactories = factories
    }

    override fun render(node: Node, output: StringBuilder) {
        val context = RendererContext(HtmlWriter(output))
        context.beforeRoot(node)
        context.render(node)
        context.afterRoot(node)
    }

    override fun render(node: Node): String {
        val sb = StringBuilder()
        render(node, sb)
        return sb.toString()
    }

    /**
     * Builder for configuring an [HtmlRenderer]. See methods for default configuration.
     */
    public class Builder {

        internal var softbreak: String = "\n"
        internal var escapeHtml: Boolean = false
        internal var sanitizeUrls: Boolean = false
        internal var urlSanitizer: UrlSanitizer = DefaultUrlSanitizer()
        internal var percentEncodeUrls: Boolean = false
        internal var omitSingleParagraphP: Boolean = false
        internal var attributeProviderFactories: MutableList<AttributeProviderFactory> = mutableListOf()
        internal var nodeRendererFactories: MutableList<HtmlNodeRendererFactory> = mutableListOf()

        /**
         * @return the configured [HtmlRenderer]
         */
        public fun build(): HtmlRenderer {
            return HtmlRenderer(this)
        }

        /**
         * The HTML to use for rendering a softbreak, defaults to `"\n"` (meaning the rendered result doesn't have
         * a line break).
         *
         * Set it to `"<br>"` (or `"<br />"`) to make them hard breaks.
         *
         * Set it to `" "` to ignore line wrapping in the source.
         *
         * @param softbreak HTML for softbreak
         * @return `this`
         */
        public fun softbreak(softbreak: String): Builder {
            this.softbreak = softbreak
            return this
        }

        /**
         * Whether [HtmlInline][org.commonmark.node.HtmlInline] and [HtmlBlock][org.commonmark.node.HtmlBlock] should be escaped, defaults to `false`.
         *
         * Note that [HtmlInline][org.commonmark.node.HtmlInline] is only a tag itself, not the text between an opening tag and a closing tag. So
         * markup in the text will be parsed as normal and is not affected by this option.
         *
         * @param escapeHtml true for escaping, false for preserving raw HTML
         * @return `this`
         */
        public fun escapeHtml(escapeHtml: Boolean): Builder {
            this.escapeHtml = escapeHtml
            return this
        }

        /**
         * Whether image src and link href should be sanitized, defaults to `false`.
         *
         * @param sanitizeUrls true for sanitization, false for preserving raw attribute
         * @return `this`
         */
        public fun sanitizeUrls(sanitizeUrls: Boolean): Builder {
            this.sanitizeUrls = sanitizeUrls
            return this
        }

        /**
         * [UrlSanitizer] used to filter URLs if [sanitizeUrls] is true.
         *
         * @param urlSanitizer Filterer used to filter image src and link href.
         * @return `this`
         */
        public fun urlSanitizer(urlSanitizer: UrlSanitizer): Builder {
            this.urlSanitizer = urlSanitizer
            return this
        }

        /**
         * Whether URLs of link or images should be percent-encoded, defaults to `false`.
         *
         * If enabled, the following is done:
         * - Existing percent-encoded parts are preserved (e.g. "%20" is kept as "%20")
         * - Reserved characters such as "/" are preserved, except for "[" and "]" (see encodeURI in JS)
         * - Unreserved characters such as "a" are preserved
         * - Other characters such umlauts are percent-encoded
         *
         * @param percentEncodeUrls true to percent-encode, false for leaving as-is
         * @return `this`
         */
        public fun percentEncodeUrls(percentEncodeUrls: Boolean): Builder {
            this.percentEncodeUrls = percentEncodeUrls
            return this
        }

        /**
         * Whether documents that only contain a single paragraph should be rendered without the `<p>` tag. Set to
         * `true` to render without the tag; the default of `false` always renders the tag.
         *
         * @return `this`
         */
        public fun omitSingleParagraphP(omitSingleParagraphP: Boolean): Builder {
            this.omitSingleParagraphP = omitSingleParagraphP
            return this
        }

        /**
         * Add a factory for an attribute provider for adding/changing HTML attributes to the rendered tags.
         *
         * @param attributeProviderFactory the attribute provider factory to add
         * @return `this`
         */
        public fun attributeProviderFactory(attributeProviderFactory: AttributeProviderFactory): Builder {
            this.attributeProviderFactories.add(attributeProviderFactory)
            return this
        }

        /**
         * Add a factory for instantiating a node renderer (done when rendering). This allows to override the rendering
         * of node types or define rendering for custom node types.
         *
         * If multiple node renderers for the same node type are created, the one from the factory that was added first
         * "wins". (This is how the rendering for core node types can be overridden; the default rendering comes last.)
         *
         * @param nodeRendererFactory the factory for creating a node renderer
         * @return `this`
         */
        public fun nodeRendererFactory(nodeRendererFactory: HtmlNodeRendererFactory): Builder {
            this.nodeRendererFactories.add(nodeRendererFactory)
            return this
        }

        /**
         * @param extensions extensions to use on this HTML renderer
         * @return `this`
         */
        public fun extensions(extensions: Iterable<Extension>): Builder {
            for (extension in extensions) {
                if (extension is HtmlRendererExtension) {
                    extension.extend(this)
                }
            }
            return this
        }
    }

    /**
     * Extension for [HtmlRenderer].
     */
    public interface HtmlRendererExtension : Extension {
        public fun extend(rendererBuilder: Builder)
    }

    private inner class RendererContext(
        private val htmlWriter: HtmlWriter
    ) : HtmlNodeRendererContext, AttributeProviderContext {

        private val attributeProviders: List<AttributeProvider>
        private val nodeRendererMap = NodeRendererMap()

        init {
            attributeProviders = attributeProviderFactories.map { it.create(this) }

            for (factory in nodeRendererFactories) {
                val renderer = factory.create(this)
                nodeRendererMap.add(renderer)
            }
        }

        override fun shouldEscapeHtml(): Boolean {
            return escapeHtml
        }

        override fun shouldOmitSingleParagraphP(): Boolean {
            return omitSingleParagraphP
        }

        override fun shouldSanitizeUrls(): Boolean {
            return sanitizeUrls
        }

        override fun urlSanitizer(): UrlSanitizer {
            return urlSanitizer
        }

        override fun encodeUrl(url: String): String {
            return if (percentEncodeUrls) {
                Escaping.percentEncodeUrl(url)
            } else {
                url
            }
        }

        override fun extendAttributes(node: Node, tagName: String, attributes: Map<String, String>): Map<String, String> {
            val attrs = LinkedHashMap(attributes)
            setCustomAttributes(node, tagName, attrs)
            return attrs
        }

        override fun getWriter(): HtmlWriter {
            return htmlWriter
        }

        override fun getSoftbreak(): String {
            return softbreak
        }

        override fun render(node: Node) {
            nodeRendererMap.render(node)
        }

        fun beforeRoot(node: Node) {
            nodeRendererMap.beforeRoot(node)
        }

        fun afterRoot(node: Node) {
            nodeRendererMap.afterRoot(node)
        }

        private fun setCustomAttributes(node: Node, tagName: String, attrs: MutableMap<String, String>) {
            for (attributeProvider in attributeProviders) {
                attributeProvider.setAttributes(node, tagName, attrs)
            }
        }
    }

    public companion object {
        /**
         * Create a new builder for configuring an [HtmlRenderer].
         *
         * @return a builder
         */
        public fun builder(): Builder {
            return Builder()
        }
    }
}
