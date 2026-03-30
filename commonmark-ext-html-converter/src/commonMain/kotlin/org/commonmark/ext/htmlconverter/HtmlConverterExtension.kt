package org.commonmark.ext.htmlconverter

import org.commonmark.Extension
import org.commonmark.ext.htmlconverter.internal.HtmlConverterHtmlNodeRenderer
import org.commonmark.ext.htmlconverter.internal.HtmlConverterMarkdownNodeRenderer
import org.commonmark.ext.htmlconverter.internal.HtmlConverterTextContentNodeRenderer
import org.commonmark.renderer.NodeRenderer
import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext
import org.commonmark.renderer.markdown.MarkdownNodeRendererFactory
import org.commonmark.renderer.markdown.MarkdownRenderer
import org.commonmark.renderer.text.TextContentRenderer

/**
 * Extension for converting HTML blocks and HTML inline elements within Markdown to their
 * corresponding Markdown equivalents.
 *
 * When this extension is active, `HtmlBlock` and `HtmlInline` nodes in the parsed Markdown AST
 * are converted to their Markdown equivalents during rendering, rather than being passed through
 * as raw HTML.
 *
 * Create it with [create] and then configure it on the renderers:
 * ```
 * val extensions = listOf(HtmlConverterExtension.create())
 * val renderer = MarkdownRenderer.builder().extensions(extensions).build()
 * ```
 *
 * This extension also supports direct HTML-to-Markdown conversion via [HtmlToMarkdownConverter]:
 * ```
 * val markdown = HtmlToMarkdownConverter.convert("<h1>Hello</h1>")
 * ```
 */
public class HtmlConverterExtension private constructor() :
    HtmlRenderer.HtmlRendererExtension,
    TextContentRenderer.TextContentRendererExtension,
    MarkdownRenderer.MarkdownRendererExtension {
    public companion object {
        public fun create(): Extension = HtmlConverterExtension()
    }

    override fun extend(rendererBuilder: HtmlRenderer.Builder) {
        rendererBuilder.nodeRendererFactory { context -> HtmlConverterHtmlNodeRenderer(context) }
    }

    override fun extend(rendererBuilder: TextContentRenderer.Builder) {
        rendererBuilder.nodeRendererFactory { context -> HtmlConverterTextContentNodeRenderer(context) }
    }

    override fun extend(rendererBuilder: MarkdownRenderer.Builder) {
        rendererBuilder.nodeRendererFactory(
            object : MarkdownNodeRendererFactory {
                override fun create(context: MarkdownNodeRendererContext): NodeRenderer = HtmlConverterMarkdownNodeRenderer(context)

                override fun getSpecialCharacters(): Set<Char> = emptySet()
            },
        )
    }
}
