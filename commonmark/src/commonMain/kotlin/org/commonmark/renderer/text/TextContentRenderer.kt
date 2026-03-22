package org.commonmark.renderer.text

import org.commonmark.Extension
import org.commonmark.internal.renderer.NodeRendererMap
import org.commonmark.node.Node
import org.commonmark.renderer.NodeRenderer
import org.commonmark.renderer.Renderer

/**
 * Renders nodes to plain text content with minimal markup-like additions.
 */
public class TextContentRenderer private constructor(
    builder: Builder,
) : Renderer {
    private val lineBreakRendering: LineBreakRendering = builder.lineBreakRendering
    private val nodeRendererFactories: List<TextContentNodeRendererFactory>

    init {
        val factories = ArrayList<TextContentNodeRendererFactory>(builder.nodeRendererFactories.size + 1)
        factories.addAll(builder.nodeRendererFactories)
        // Add as last. This means clients can override the rendering of core nodes if they want.
        factories.add(TextContentNodeRendererFactory { context -> CoreTextContentNodeRenderer(context) })
        this.nodeRendererFactories = factories
    }

    override fun render(
        node: Node,
        output: StringBuilder,
    ) {
        val context = RendererContext(TextContentWriter(output, lineBreakRendering))
        context.render(node)
    }

    override fun render(node: Node): String {
        val sb = StringBuilder()
        render(node, sb)
        return sb.toString()
    }

    /**
     * Builder for configuring a [TextContentRenderer]. See methods for default configuration.
     */
    public class Builder {
        internal var nodeRendererFactories: MutableList<TextContentNodeRendererFactory> = mutableListOf()
        internal var lineBreakRendering: LineBreakRendering = LineBreakRendering.COMPACT

        /**
         * @return the configured [TextContentRenderer]
         */
        public fun build(): TextContentRenderer = TextContentRenderer(this)

        /**
         * Configure how line breaks (newlines) are rendered, see [LineBreakRendering].
         * The default is [LineBreakRendering.COMPACT].
         *
         * @param lineBreakRendering the mode to use
         * @return `this`
         */
        public fun lineBreakRendering(lineBreakRendering: LineBreakRendering): Builder {
            this.lineBreakRendering = lineBreakRendering
            return this
        }

        /**
         * Set the value of flag for stripping new lines.
         *
         * @param stripNewlines true for stripping new lines and render text as "single line",
         *                      false for keeping all line breaks
         * @return `this`
         */
        @Deprecated("Use lineBreakRendering(LineBreakRendering) with LineBreakRendering.STRIP instead")
        public fun stripNewlines(stripNewlines: Boolean): Builder {
            this.lineBreakRendering = if (stripNewlines) LineBreakRendering.STRIP else LineBreakRendering.COMPACT
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
        public fun nodeRendererFactory(nodeRendererFactory: TextContentNodeRendererFactory): Builder {
            this.nodeRendererFactories.add(nodeRendererFactory)
            return this
        }

        /**
         * @param extensions extensions to use on this text content renderer
         * @return `this`
         */
        public fun extensions(extensions: Iterable<Extension>): Builder {
            for (extension in extensions) {
                if (extension is TextContentRendererExtension) {
                    extension.extend(this)
                }
            }
            return this
        }
    }

    /**
     * Extension for [TextContentRenderer].
     */
    public interface TextContentRendererExtension : Extension {
        public fun extend(rendererBuilder: Builder)
    }

    private inner class RendererContext(
        private val textContentWriter: TextContentWriter,
    ) : TextContentNodeRendererContext {
        private val nodeRendererMap = NodeRendererMap()

        init {
            for (factory in nodeRendererFactories) {
                val renderer = factory.create(this)
                nodeRendererMap.add(renderer)
            }
        }

        override fun lineBreakRendering(): LineBreakRendering = lineBreakRendering

        @Suppress("DEPRECATION")
        override fun stripNewlines(): Boolean = lineBreakRendering == LineBreakRendering.STRIP

        override fun getWriter(): TextContentWriter = textContentWriter

        override fun render(node: Node) {
            nodeRendererMap.render(node)
        }
    }

    public companion object {
        /**
         * Create a new builder for configuring a [TextContentRenderer].
         *
         * @return a builder
         */
        public fun builder(): Builder = Builder()
    }
}
