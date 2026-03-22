package org.commonmark.ext.heading.anchor.internal

import org.commonmark.ext.heading.anchor.IdGenerator
import org.commonmark.node.AbstractVisitor
import org.commonmark.node.Code
import org.commonmark.node.Heading
import org.commonmark.node.Node
import org.commonmark.node.Text
import org.commonmark.renderer.html.AttributeProvider

internal class HeadingIdAttributeProvider private constructor(
    defaultId: String,
    prefix: String,
    suffix: String,
) : AttributeProvider {
    private val idGenerator: IdGenerator =
        IdGenerator
            .builder()
            .defaultId(defaultId)
            .prefix(prefix)
            .suffix(suffix)
            .build()

    override fun setAttributes(
        node: Node,
        tagName: String,
        attributes: MutableMap<String, String?>,
    ) {
        if (node is Heading) {
            val wordList = mutableListOf<String>()

            node.accept(
                object : AbstractVisitor() {
                    override fun visit(text: Text) {
                        wordList.add(text.literal)
                    }

                    override fun visit(code: Code) {
                        wordList.add(code.literal)
                    }
                },
            )

            val finalString = wordList.joinToString("").trim().lowercase()
            attributes["id"] = idGenerator.generateId(finalString)
        }
    }

    companion object {
        fun create(
            defaultId: String,
            prefix: String,
            suffix: String,
        ): HeadingIdAttributeProvider = HeadingIdAttributeProvider(defaultId, prefix, suffix)
    }
}
