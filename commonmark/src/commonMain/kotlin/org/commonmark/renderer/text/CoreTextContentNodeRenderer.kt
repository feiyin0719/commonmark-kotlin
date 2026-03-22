package org.commonmark.renderer.text

import org.commonmark.node.*
import org.commonmark.renderer.NodeRenderer
import kotlin.reflect.KClass

/**
 * The node renderer that renders all the core nodes (comes last in the order of node renderers).
 */
public class CoreTextContentNodeRenderer(
    protected val context: TextContentNodeRendererContext,
) : AbstractVisitor(),
    NodeRenderer {
    private val textContent: TextContentWriter = context.getWriter()
    private var listHolder: ListHolder? = null

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

    override fun visit(blockQuote: BlockQuote) {
        // LEFT-POINTING DOUBLE ANGLE QUOTATION MARK
        textContent.write('\u00AB')
        visitChildren(blockQuote)
        textContent.resetBlock()
        // RIGHT-POINTING DOUBLE ANGLE QUOTATION MARK
        textContent.write('\u00BB')

        textContent.block()
    }

    override fun visit(bulletList: BulletList) {
        textContent.pushTight(bulletList.isTight)
        listHolder = BulletListHolder(listHolder, bulletList)
        visitChildren(bulletList)
        textContent.popTight()
        textContent.block()
        listHolder = listHolder?.parent
    }

    override fun visit(code: Code) {
        textContent.write('"')
        textContent.write(code.literal)
        textContent.write('"')
    }

    override fun visit(fencedCodeBlock: FencedCodeBlock) {
        val literal = stripTrailingNewline(fencedCodeBlock.literal ?: "")
        if (stripNewlines()) {
            textContent.writeStripped(literal)
        } else {
            textContent.write(literal)
        }
        textContent.block()
    }

    override fun visit(hardLineBreak: HardLineBreak) {
        if (stripNewlines()) {
            textContent.whitespace()
        } else {
            textContent.line()
        }
    }

    override fun visit(heading: Heading) {
        visitChildren(heading)
        if (stripNewlines()) {
            textContent.write(": ")
        } else {
            textContent.block()
        }
    }

    override fun visit(thematicBreak: ThematicBreak) {
        if (!stripNewlines()) {
            textContent.write("***")
        }
        textContent.block()
    }

    override fun visit(htmlInline: HtmlInline) {
        writeText(htmlInline.literal ?: "")
    }

    override fun visit(htmlBlock: HtmlBlock) {
        writeText(htmlBlock.literal ?: "")
    }

    override fun visit(image: Image) {
        writeLink(image, image.title, image.destination)
    }

    override fun visit(indentedCodeBlock: IndentedCodeBlock) {
        val literal = stripTrailingNewline(indentedCodeBlock.literal ?: "")
        if (stripNewlines()) {
            textContent.writeStripped(literal)
        } else {
            textContent.write(literal)
        }
        textContent.block()
    }

    override fun visit(link: Link) {
        writeLink(link, link.title, link.destination)
    }

    override fun visit(listItem: ListItem) {
        val holder = listHolder
        if (holder is OrderedListHolder) {
            val marker = "${holder.counter}${holder.delimiter}"
            val spaces = " "
            textContent.write(marker)
            textContent.write(spaces)
            textContent.pushPrefix(" ".repeat(marker.length + spaces.length))
            visitChildren(listItem)
            textContent.block()
            textContent.popPrefix()
            holder.increaseCounter()
        } else if (holder is BulletListHolder) {
            if (!stripNewlines()) {
                val marker = holder.marker
                val spaces = " "
                textContent.write(marker)
                textContent.write(spaces)
                textContent.pushPrefix(" ".repeat(marker.length + spaces.length))
            }
            visitChildren(listItem)
            textContent.block()
            if (!stripNewlines()) {
                textContent.popPrefix()
            }
        }
    }

    override fun visit(orderedList: OrderedList) {
        textContent.pushTight(orderedList.isTight)
        listHolder = OrderedListHolder(listHolder, orderedList)
        visitChildren(orderedList)
        textContent.popTight()
        textContent.block()
        listHolder = listHolder?.parent
    }

    override fun visit(paragraph: Paragraph) {
        visitChildren(paragraph)
        textContent.block()
    }

    override fun visit(softLineBreak: SoftLineBreak) {
        if (stripNewlines()) {
            textContent.whitespace()
        } else {
            textContent.line()
        }
    }

    override fun visit(text: Text) {
        writeText(text.literal)
    }

    override fun visitChildren(parent: Node) {
        var node = parent.firstChild
        while (node != null) {
            val next = node.next
            context.render(node)
            node = next
        }
    }

    private fun writeText(text: String) {
        if (stripNewlines()) {
            textContent.writeStripped(text)
        } else {
            textContent.write(text)
        }
    }

    private fun writeLink(
        node: Node,
        title: String?,
        destination: String?,
    ) {
        val hasChild = node.firstChild != null
        val hasTitle = title != null && title != destination
        val hasDestination = destination != null && destination != ""

        if (hasChild) {
            textContent.write('"')
            visitChildren(node)
            textContent.write('"')
            if (hasTitle || hasDestination) {
                textContent.whitespace()
                textContent.write('(')
            }
        }

        if (hasTitle) {
            textContent.write(title!!)
            if (hasDestination) {
                textContent.colon()
                textContent.whitespace()
            }
        }

        if (hasDestination) {
            textContent.write(destination!!)
        }

        if (hasChild && (hasTitle || hasDestination)) {
            textContent.write(')')
        }
    }

    private fun stripNewlines(): Boolean = context.lineBreakRendering() == LineBreakRendering.STRIP

    private abstract class ListHolder(
        val parent: ListHolder?,
    )

    private class BulletListHolder(
        parent: ListHolder?,
        list: BulletList,
    ) : ListHolder(parent) {
        val marker: String = list.marker ?: "-"
    }

    private class OrderedListHolder(
        parent: ListHolder?,
        list: OrderedList,
    ) : ListHolder(parent) {
        val delimiter: String = list.markerDelimiter ?: "."
        var counter: Int = list.markerStartNumber ?: 1

        fun increaseCounter() {
            counter++
        }
    }

    private companion object {
        fun stripTrailingNewline(s: String): String =
            if (s.endsWith("\n")) {
                s.substring(0, s.length - 1)
            } else {
                s
            }
    }
}
