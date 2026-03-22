package org.commonmark.ext.task.list.items.internal

import org.commonmark.ext.task.list.items.TaskListItemMarker
import org.commonmark.node.Node
import org.commonmark.renderer.NodeRenderer
import org.commonmark.renderer.html.HtmlNodeRendererContext
import org.commonmark.renderer.html.HtmlWriter
import kotlin.reflect.KClass

internal class TaskListItemHtmlNodeRenderer(
    private val context: HtmlNodeRendererContext,
) : NodeRenderer {
    private val html: HtmlWriter = context.getWriter()

    override fun getNodeTypes(): Set<KClass<out Node>> = setOf(TaskListItemMarker::class)

    override fun render(node: Node) {
        if (node is TaskListItemMarker) {
            val attributes = linkedMapOf<String, String>()
            attributes["type"] = "checkbox"
            attributes["disabled"] = ""
            if (node.isChecked) {
                attributes["checked"] = ""
            }
            html.tag("input", context.extendAttributes(node, "input", attributes))
            // Add a space after the input tag (as the next text node has been trimmed)
            html.text(" ")
            renderChildren(node)
        }
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
