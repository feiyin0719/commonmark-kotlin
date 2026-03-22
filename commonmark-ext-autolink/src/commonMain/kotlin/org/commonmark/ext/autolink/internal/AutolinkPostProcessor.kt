package org.commonmark.ext.autolink.internal

import org.commonmark.ext.autolink.AutolinkType
import org.commonmark.node.AbstractVisitor
import org.commonmark.node.Link
import org.commonmark.node.Node
import org.commonmark.node.SourceSpan
import org.commonmark.node.Text
import org.commonmark.parser.PostProcessor

/**
 * Post-processor that walks the AST to find [Text] nodes that are not inside [Link] nodes, and
 * wraps detected URLs and email addresses in [Link] nodes.
 */
internal class AutolinkPostProcessor(
    linkTypes: Set<AutolinkType>,
) : PostProcessor {
    private val linkExtractor: LinkExtractor

    init {
        require(linkTypes.isNotEmpty()) { "linkTypes must not be empty" }
        linkExtractor = LinkExtractor(linkTypes)
    }

    override fun process(node: Node): Node {
        val autolinkVisitor = AutolinkVisitor()
        node.accept(autolinkVisitor)
        return node
    }

    private fun linkify(originalTextNode: Text) {
        val literal = originalTextNode.literal

        var lastNode: Node = originalTextNode
        val sourceSpans = originalTextNode.getSourceSpans()
        val sourceSpan: SourceSpan? = if (sourceSpans.size == 1) sourceSpans[0] else null

        val spans = linkExtractor.extractSpans(literal)
        val iterator = spans.iterator()
        var index = 0

        while (iterator.hasNext()) {
            val span = iterator.next()
            val isFirst = (index == 0)
            val isLast = !iterator.hasNext()

            if (isFirst && lastNode === originalTextNode && isLast && span !is LinkSpan) {
                // Didn't find any links, don't bother changing the existing node.
                return
            }

            val textNode = createTextNode(literal, span, sourceSpan)
            if (span is LinkSpan) {
                val destination = getDestination(span, textNode.literal)

                val linkNode = Link(destination, null)
                linkNode.appendChild(textNode)
                linkNode.setSourceSpans(textNode.getSourceSpans())
                lastNode = insertNode(linkNode, lastNode)
            } else {
                lastNode = insertNode(textNode, lastNode)
            }
            index++
        }

        // Original node no longer needed
        originalTextNode.unlink()
    }

    private inner class AutolinkVisitor : AbstractVisitor() {
        private var inLink = 0

        override fun visit(link: Link) {
            inLink++
            super.visit(link)
            inLink--
        }

        override fun visit(text: Text) {
            if (inLink == 0) {
                linkify(text)
            }
        }
    }

    companion object {
        private fun createTextNode(
            literal: String,
            span: Span,
            sourceSpan: SourceSpan?,
        ): Text {
            val beginIndex = span.beginIndex
            val endIndex = span.endIndex
            val text = literal.substring(beginIndex, endIndex)
            val textNode = Text(text)
            if (sourceSpan != null) {
                textNode.addSourceSpan(sourceSpan.subSpan(beginIndex, endIndex))
            }
            return textNode
        }

        private fun getDestination(
            linkSpan: LinkSpan,
            linkText: String,
        ): String =
            when (linkSpan.type) {
                LinkType.EMAIL -> "mailto:$linkText"
                LinkType.WWW -> "http://$linkText"
                LinkType.URL -> linkText
            }

        private fun insertNode(
            node: Node,
            insertAfterNode: Node,
        ): Node {
            insertAfterNode.insertAfter(node)
            return node
        }
    }
}
