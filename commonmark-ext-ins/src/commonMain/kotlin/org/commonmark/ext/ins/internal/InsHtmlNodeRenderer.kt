package org.commonmark.ext.ins.internal

import org.commonmark.node.Node
import org.commonmark.renderer.html.HtmlNodeRendererContext
import org.commonmark.renderer.html.HtmlWriter

internal class InsHtmlNodeRenderer(
    private val context: HtmlNodeRendererContext,
) : InsNodeRenderer() {
    private val html: HtmlWriter = context.getWriter()

    override fun render(node: Node) {
        val attributes = context.extendAttributes(node, "ins", emptyMap())
        html.tag("ins", attributes)
        renderChildren(node)
        html.tag("/ins")
    }

    private fun renderChildren(parent: Node) {
        var node = parent.firstChild
        while (node != null) {
            val next = node.next
            context.render(node)
            node = next
        }
    }
}
