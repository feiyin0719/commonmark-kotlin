package org.commonmark.ext.htmlconverter.internal

import org.commonmark.ext.htmlconverter.HtmlToMarkdownConverter
import org.commonmark.node.HtmlBlock
import org.commonmark.node.HtmlInline
import org.commonmark.node.Node
import org.commonmark.renderer.html.HtmlNodeRendererContext
import org.commonmark.renderer.html.HtmlWriter

/**
 * Renders HtmlBlock and HtmlInline nodes by first converting the HTML content
 * to Markdown AST nodes, then rendering those nodes using the HTML renderer context.
 */
internal class HtmlConverterHtmlNodeRenderer(
    private val context: HtmlNodeRendererContext,
) : HtmlConverterNodeRenderer() {
    private val html: HtmlWriter = context.getWriter()

    override fun render(node: Node) {
        when (node) {
            is HtmlBlock -> {
                val literal = node.literal ?: return
                val document = HtmlToMarkdownConverter.convertToDocument(literal)
                // Render the converted document's children through the normal rendering pipeline
                var child = document.firstChild
                while (child != null) {
                    val next = child.next
                    context.render(child)
                    child = next
                }
            }

            is HtmlInline -> {
                // For inline HTML, pass through as-is since the HTML renderer
                // already handles HTML output appropriately
                val literal = node.literal ?: return
                if (context.shouldEscapeHtml()) {
                    html.text(literal)
                } else {
                    html.raw(literal)
                }
            }
        }
    }
}
