package org.commonmark.ext.htmlconverter.internal

import org.commonmark.ext.htmlconverter.HtmlToMarkdownConverter
import org.commonmark.node.HtmlBlock
import org.commonmark.node.HtmlInline
import org.commonmark.node.Node
import org.commonmark.renderer.text.TextContentNodeRendererContext
import org.commonmark.renderer.text.TextContentWriter

/**
 * Renders HtmlBlock and HtmlInline nodes as plain text by converting
 * the HTML content to Markdown AST nodes first, then rendering as text.
 */
internal class HtmlConverterTextContentNodeRenderer(
    private val context: TextContentNodeRendererContext,
) : HtmlConverterNodeRenderer() {
    private val textContent: TextContentWriter = context.getWriter()

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
                // For inline HTML in text output, strip all tags
                val literal = node.literal ?: return
                val trimmed = literal.trim()
                // Only output content for <br> as a space
                if (trimmed.equals("<br>", ignoreCase = true) ||
                    trimmed.equals("<br/>", ignoreCase = true) ||
                    trimmed.equals("<br />", ignoreCase = true)
                ) {
                    textContent.write(' ')
                }
                // All other tags are stripped
            }
        }
    }
}
