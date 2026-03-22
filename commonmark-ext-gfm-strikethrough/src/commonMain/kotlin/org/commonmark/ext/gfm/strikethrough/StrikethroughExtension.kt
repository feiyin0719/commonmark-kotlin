package org.commonmark.ext.gfm.strikethrough

import org.commonmark.Extension
import org.commonmark.ext.gfm.strikethrough.internal.StrikethroughDelimiterProcessor
import org.commonmark.ext.gfm.strikethrough.internal.StrikethroughHtmlNodeRenderer
import org.commonmark.ext.gfm.strikethrough.internal.StrikethroughMarkdownNodeRenderer
import org.commonmark.ext.gfm.strikethrough.internal.StrikethroughTextContentNodeRenderer
import org.commonmark.parser.Parser
import org.commonmark.renderer.NodeRenderer
import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext
import org.commonmark.renderer.markdown.MarkdownNodeRendererFactory
import org.commonmark.renderer.markdown.MarkdownRenderer
import org.commonmark.renderer.text.TextContentRenderer

/**
 * Extension for GFM strikethrough using `~` or `~~` (GitHub Flavored Markdown).
 *
 * Create it with [create] and then configure it on the builders
 * ([Parser.Builder.extensions], [HtmlRenderer.Builder.extensions]).
 *
 * The parsed strikethrough text regions are turned into [Strikethrough] nodes.
 */
public class StrikethroughExtension private constructor(
    builder: Builder,
) : Parser.ParserExtension,
    HtmlRenderer.HtmlRendererExtension,
    TextContentRenderer.TextContentRendererExtension,
    MarkdownRenderer.MarkdownRendererExtension {
    private val requireTwoTildes: Boolean = builder.requireTwoTildes

    public companion object {
        public fun create(): Extension = builder().build()

        public fun builder(): Builder = Builder()
    }

    override fun extend(parserBuilder: Parser.Builder) {
        parserBuilder.customDelimiterProcessor(StrikethroughDelimiterProcessor(requireTwoTildes))
    }

    override fun extend(rendererBuilder: HtmlRenderer.Builder) {
        rendererBuilder.nodeRendererFactory { context -> StrikethroughHtmlNodeRenderer(context) }
    }

    override fun extend(rendererBuilder: TextContentRenderer.Builder) {
        rendererBuilder.nodeRendererFactory { context -> StrikethroughTextContentNodeRenderer(context) }
    }

    override fun extend(rendererBuilder: MarkdownRenderer.Builder) {
        rendererBuilder.nodeRendererFactory(
            object : MarkdownNodeRendererFactory {
                override fun create(context: MarkdownNodeRendererContext): NodeRenderer = StrikethroughMarkdownNodeRenderer(context)

                override fun getSpecialCharacters(): Set<Char> = setOf('~')
            },
        )
    }

    public class Builder {
        internal var requireTwoTildes: Boolean = false

        public fun requireTwoTildes(requireTwoTildes: Boolean): Builder {
            this.requireTwoTildes = requireTwoTildes
            return this
        }

        public fun build(): Extension = StrikethroughExtension(this)
    }
}
