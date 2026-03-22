package org.commonmark.ext.ins

import org.commonmark.Extension
import org.commonmark.ext.ins.internal.InsDelimiterProcessor
import org.commonmark.ext.ins.internal.InsHtmlNodeRenderer
import org.commonmark.ext.ins.internal.InsMarkdownNodeRenderer
import org.commonmark.ext.ins.internal.InsTextContentNodeRenderer
import org.commonmark.parser.Parser
import org.commonmark.renderer.NodeRenderer
import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext
import org.commonmark.renderer.markdown.MarkdownNodeRendererFactory
import org.commonmark.renderer.markdown.MarkdownRenderer
import org.commonmark.renderer.text.TextContentRenderer

/**
 * Extension for ins using `++`.
 *
 * Create it with [create] and then configure it on the builders
 * ([Parser.Builder.extensions], [HtmlRenderer.Builder.extensions]).
 *
 * The parsed ins text regions are turned into [Ins] nodes.
 */
public class InsExtension private constructor() :
    Parser.ParserExtension,
    HtmlRenderer.HtmlRendererExtension,
    TextContentRenderer.TextContentRendererExtension,
    MarkdownRenderer.MarkdownRendererExtension {
        public companion object {
            public fun create(): Extension = InsExtension()
        }

        override fun extend(parserBuilder: Parser.Builder) {
            parserBuilder.customDelimiterProcessor(InsDelimiterProcessor())
        }

        override fun extend(rendererBuilder: HtmlRenderer.Builder) {
            rendererBuilder.nodeRendererFactory { context -> InsHtmlNodeRenderer(context) }
        }

        override fun extend(rendererBuilder: TextContentRenderer.Builder) {
            rendererBuilder.nodeRendererFactory { context -> InsTextContentNodeRenderer(context) }
        }

        override fun extend(rendererBuilder: MarkdownRenderer.Builder) {
            rendererBuilder.nodeRendererFactory(
                object : MarkdownNodeRendererFactory {
                    override fun create(context: MarkdownNodeRendererContext): NodeRenderer = InsMarkdownNodeRenderer(context)

                    override fun getSpecialCharacters(): Set<Char> = setOf('+')
                },
            )
        }
    }
