package org.commonmark.renderer.html

import org.commonmark.node.*
import org.commonmark.renderer.NodeRenderer
import kotlin.reflect.KClass

/**
 * The node renderer that renders all the core nodes (comes last in the order of node renderers).
 */
public class CoreHtmlNodeRenderer(
    protected val context: HtmlNodeRendererContext,
) : AbstractVisitor(),
    NodeRenderer {
    private val html: HtmlWriter = context.getWriter()

    override fun getNodeTypes(): Set<KClass<out Node>> =
        setOf(
            Document::class,
            Heading::class,
            Paragraph::class,
            BlockQuote::class,
            BulletList::class,
            FencedCodeBlock::class,
            HtmlBlock::class,
            ThematicBreak::class,
            IndentedCodeBlock::class,
            Link::class,
            ListItem::class,
            OrderedList::class,
            Image::class,
            Emphasis::class,
            StrongEmphasis::class,
            Text::class,
            Code::class,
            HtmlInline::class,
            SoftLineBreak::class,
            HardLineBreak::class,
        )

    override fun render(node: Node) {
        node.accept(this)
    }

    override fun visit(document: Document) {
        // No rendering itself
        visitChildren(document)
    }

    override fun visit(heading: Heading) {
        val htag = "h${heading.level}"
        html.line()
        html.tag(htag, getAttrs(heading, htag))
        visitChildren(heading)
        html.tag("/$htag")
        html.line()
    }

    override fun visit(paragraph: Paragraph) {
        val omitP =
            isInTightList(paragraph) ||
                (
                    context.shouldOmitSingleParagraphP() && paragraph.parent is Document &&
                        paragraph.previous == null && paragraph.next == null
                )
        if (!omitP) {
            html.line()
            html.tag("p", getAttrs(paragraph, "p"))
        }
        visitChildren(paragraph)
        if (!omitP) {
            html.tag("/p")
            html.line()
        }
    }

    override fun visit(blockQuote: BlockQuote) {
        html.line()
        html.tag("blockquote", getAttrs(blockQuote, "blockquote"))
        html.line()
        visitChildren(blockQuote)
        html.line()
        html.tag("/blockquote")
        html.line()
    }

    override fun visit(bulletList: BulletList) {
        renderListBlock(bulletList, "ul", getAttrs(bulletList, "ul"))
    }

    override fun visit(fencedCodeBlock: FencedCodeBlock) {
        val literal = fencedCodeBlock.literal ?: ""
        val attributes = linkedMapOf<String, String>()
        val info = fencedCodeBlock.info
        if (info != null && info.isNotEmpty()) {
            val space = info.indexOf(" ")
            val language: String =
                if (space == -1) {
                    info
                } else {
                    info.substring(0, space)
                }
            attributes["class"] = "language-$language"
        }
        renderCodeBlock(literal, fencedCodeBlock, attributes)
    }

    override fun visit(htmlBlock: HtmlBlock) {
        html.line()
        if (context.shouldEscapeHtml()) {
            html.tag("p", getAttrs(htmlBlock, "p"))
            html.text(htmlBlock.literal ?: "")
            html.tag("/p")
        } else {
            html.raw(htmlBlock.literal ?: "")
        }
        html.line()
    }

    override fun visit(thematicBreak: ThematicBreak) {
        html.line()
        html.tag("hr", getAttrs(thematicBreak, "hr"), true)
        html.line()
    }

    override fun visit(indentedCodeBlock: IndentedCodeBlock) {
        renderCodeBlock(indentedCodeBlock.literal ?: "", indentedCodeBlock, emptyMap())
    }

    override fun visit(link: Link) {
        val attrs = linkedMapOf<String, String>()
        var url = link.destination

        if (context.shouldSanitizeUrls()) {
            url = context.urlSanitizer().sanitizeLinkUrl(url)
            attrs["rel"] = "nofollow"
        }

        url = context.encodeUrl(url)
        attrs["href"] = url
        if (link.title != null) {
            attrs["title"] = link.title!!
        }
        html.tag("a", getAttrs(link, "a", attrs))
        visitChildren(link)
        html.tag("/a")
    }

    override fun visit(listItem: ListItem) {
        html.tag("li", getAttrs(listItem, "li"))
        visitChildren(listItem)
        html.tag("/li")
        html.line()
    }

    override fun visit(orderedList: OrderedList) {
        val start = orderedList.markerStartNumber ?: 1
        val attrs = linkedMapOf<String, String>()
        if (start != 1) {
            attrs["start"] = start.toString()
        }
        renderListBlock(orderedList, "ol", getAttrs(orderedList, "ol", attrs))
    }

    override fun visit(image: Image) {
        var url = image.destination

        val altTextVisitor = AltTextVisitor()
        image.accept(altTextVisitor)
        val altText = altTextVisitor.getAltText()

        val attrs = linkedMapOf<String, String>()
        if (context.shouldSanitizeUrls()) {
            url = context.urlSanitizer().sanitizeImageUrl(url)
        }

        attrs["src"] = context.encodeUrl(url)
        attrs["alt"] = altText
        if (image.title != null) {
            attrs["title"] = image.title!!
        }

        html.tag("img", getAttrs(image, "img", attrs), true)
    }

    override fun visit(emphasis: Emphasis) {
        html.tag("em", getAttrs(emphasis, "em"))
        visitChildren(emphasis)
        html.tag("/em")
    }

    override fun visit(strongEmphasis: StrongEmphasis) {
        html.tag("strong", getAttrs(strongEmphasis, "strong"))
        visitChildren(strongEmphasis)
        html.tag("/strong")
    }

    override fun visit(text: Text) {
        html.text(text.literal)
    }

    override fun visit(code: Code) {
        html.tag("code", getAttrs(code, "code"))
        html.text(code.literal)
        html.tag("/code")
    }

    override fun visit(htmlInline: HtmlInline) {
        if (context.shouldEscapeHtml()) {
            html.text(htmlInline.literal ?: "")
        } else {
            html.raw(htmlInline.literal ?: "")
        }
    }

    override fun visit(softLineBreak: SoftLineBreak) {
        html.raw(context.getSoftbreak())
    }

    override fun visit(hardLineBreak: HardLineBreak) {
        html.tag("br", getAttrs(hardLineBreak, "br"), true)
        html.line()
    }

    override fun visitChildren(parent: Node) {
        var node = parent.firstChild
        while (node != null) {
            val next = node.next
            context.render(node)
            node = next
        }
    }

    private fun renderCodeBlock(
        literal: String,
        node: Node,
        attributes: Map<String, String?>,
    ) {
        html.line()
        html.tag("pre", getAttrs(node, "pre"))
        html.tag("code", getAttrs(node, "code", attributes))
        html.text(literal)
        html.tag("/code")
        html.tag("/pre")
        html.line()
    }

    private fun renderListBlock(
        listBlock: ListBlock,
        tagName: String,
        attributes: Map<String, String?>,
    ) {
        html.line()
        html.tag(tagName, attributes)
        html.line()
        visitChildren(listBlock)
        html.line()
        html.tag("/$tagName")
        html.line()
    }

    private fun isInTightList(paragraph: Paragraph): Boolean {
        val parent = paragraph.parent
        if (parent != null) {
            val gramps = parent.parent
            if (gramps is ListBlock) {
                return gramps.isTight
            }
        }
        return false
    }

    private fun getAttrs(
        node: Node,
        tagName: String,
    ): Map<String, String?> = getAttrs(node, tagName, emptyMap())

    private fun getAttrs(
        node: Node,
        tagName: String,
        defaultAttributes: Map<String, String?>,
    ): Map<String, String?> = context.extendAttributes(node, tagName, defaultAttributes)

    private class AltTextVisitor : AbstractVisitor() {
        private val sb = StringBuilder()

        fun getAltText(): String = sb.toString()

        override fun visit(text: Text) {
            sb.append(text.literal)
        }

        override fun visit(code: Code) {
            sb.append(code.literal)
        }

        override fun visit(softLineBreak: SoftLineBreak) {
            sb.append('\n')
        }

        override fun visit(hardLineBreak: HardLineBreak) {
            sb.append('\n')
        }
    }
}
