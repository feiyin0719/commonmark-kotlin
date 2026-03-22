package org.commonmark.ext.image.attributes.internal

import org.commonmark.ext.image.attributes.ImageAttributes
import org.commonmark.node.AbstractVisitor
import org.commonmark.node.CustomNode
import org.commonmark.node.Image
import org.commonmark.node.Node
import org.commonmark.renderer.html.AttributeProvider

internal class ImageAttributesAttributeProvider private constructor() : AttributeProvider {
    companion object {
        fun create(): ImageAttributesAttributeProvider = ImageAttributesAttributeProvider()
    }

    override fun setAttributes(
        node: Node,
        tagName: String,
        attributes: MutableMap<String, String?>,
    ) {
        if (node is Image) {
            node.accept(
                object : AbstractVisitor() {
                    override fun visit(customNode: CustomNode) {
                        if (customNode is ImageAttributes) {
                            for ((key, value) in customNode.attributes) {
                                attributes[key] = value
                            }
                            // Now that we have used the image attributes we remove the node.
                            customNode.unlink()
                        }
                    }
                },
            )
        }
    }
}
