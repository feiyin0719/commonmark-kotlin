package org.commonmark.ext.htmlconverter.internal

import org.commonmark.ext.htmlconverter.HtmlToMarkdownConverter
import org.commonmark.node.HtmlBlock
import org.commonmark.node.HtmlInline
import org.commonmark.node.Node
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext
import org.commonmark.renderer.markdown.MarkdownWriter

/**
 * Renders HtmlBlock and HtmlInline nodes as Markdown by converting
 * the HTML content to Markdown AST nodes first.
 */
internal class HtmlConverterMarkdownNodeRenderer(
    private val context: MarkdownNodeRendererContext,
) : HtmlConverterNodeRenderer() {
    private val writer: MarkdownWriter = context.getWriter()

    override fun render(node: Node) {
        when (node) {
            is HtmlBlock -> {
                val literal = node.literal ?: return
                val document = HtmlToMarkdownConverter.convertToDocument(literal)
                var child = document.firstChild
                while (child != null) {
                    val next = child.next
                    context.render(child)
                    child = next
                }
            }

            is HtmlInline -> {
                // For inline HTML in markdown output, convert inline tags to their
                // markdown equivalents when possible
                val literal = node.literal ?: return
                val converted = convertInlineHtml(literal)
                writer.raw(converted)
            }
        }
    }

    private fun convertInlineHtml(html: String): String {
        val trimmed = html.trim()
        return when {
            trimmed.equals("<strong>", ignoreCase = true) || trimmed.equals("<b>", ignoreCase = true) -> "**"
            trimmed.equals("</strong>", ignoreCase = true) || trimmed.equals("</b>", ignoreCase = true) -> "**"
            trimmed.equals("<em>", ignoreCase = true) || trimmed.equals("<i>", ignoreCase = true) -> "*"
            trimmed.equals("</em>", ignoreCase = true) || trimmed.equals("</i>", ignoreCase = true) -> "*"
            trimmed.equals("<code>", ignoreCase = true) -> "`"
            trimmed.equals("</code>", ignoreCase = true) -> "`"
            trimmed.equals("<br>", ignoreCase = true) || trimmed.equals("<br/>", ignoreCase = true) || trimmed.equals("<br />", ignoreCase = true) -> "  \n"
            trimmed.equals("<hr>", ignoreCase = true) || trimmed.equals("<hr/>", ignoreCase = true) || trimmed.equals("<hr />", ignoreCase = true) -> "---\n"
            else -> html // Pass through unknown inline HTML
        }
    }
}
