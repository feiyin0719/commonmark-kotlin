package org.commonmark.ext.front.matter

import org.commonmark.node.AbstractVisitor
import org.commonmark.node.CustomNode

public class YamlFrontMatterVisitor : AbstractVisitor() {
    public val data: MutableMap<String, List<String>> = linkedMapOf()

    override fun visit(customNode: CustomNode) {
        if (customNode is YamlFrontMatterNode) {
            data[customNode.key] = customNode.values
        } else {
            super.visit(customNode)
        }
    }
}
