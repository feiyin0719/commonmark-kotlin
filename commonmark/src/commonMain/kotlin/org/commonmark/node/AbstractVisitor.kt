package org.commonmark.node

public abstract class AbstractVisitor : Visitor {
    override fun visit(blockQuote: BlockQuote): Unit = visitChildren(blockQuote)

    override fun visit(bulletList: BulletList): Unit = visitChildren(bulletList)

    override fun visit(code: Code): Unit = visitChildren(code)

    override fun visit(document: Document): Unit = visitChildren(document)

    override fun visit(emphasis: Emphasis): Unit = visitChildren(emphasis)

    override fun visit(fencedCodeBlock: FencedCodeBlock): Unit = visitChildren(fencedCodeBlock)

    override fun visit(hardLineBreak: HardLineBreak): Unit = visitChildren(hardLineBreak)

    override fun visit(heading: Heading): Unit = visitChildren(heading)

    override fun visit(thematicBreak: ThematicBreak): Unit = visitChildren(thematicBreak)

    override fun visit(htmlInline: HtmlInline): Unit = visitChildren(htmlInline)

    override fun visit(htmlBlock: HtmlBlock): Unit = visitChildren(htmlBlock)

    override fun visit(image: Image): Unit = visitChildren(image)

    override fun visit(indentedCodeBlock: IndentedCodeBlock): Unit = visitChildren(indentedCodeBlock)

    override fun visit(link: Link): Unit = visitChildren(link)

    override fun visit(listItem: ListItem): Unit = visitChildren(listItem)

    override fun visit(orderedList: OrderedList): Unit = visitChildren(orderedList)

    override fun visit(paragraph: Paragraph): Unit = visitChildren(paragraph)

    override fun visit(softLineBreak: SoftLineBreak): Unit = visitChildren(softLineBreak)

    override fun visit(strongEmphasis: StrongEmphasis): Unit = visitChildren(strongEmphasis)

    override fun visit(text: Text): Unit = visitChildren(text)

    override fun visit(linkReferenceDefinition: LinkReferenceDefinition): Unit = visitChildren(linkReferenceDefinition)

    override fun visit(customBlock: CustomBlock): Unit = visitChildren(customBlock)

    override fun visit(customNode: CustomNode): Unit = visitChildren(customNode)

    protected open fun visitChildren(parent: Node) {
        var node = parent.firstChild
        while (node != null) {
            val next = node.next
            node.accept(this)
            node = next
        }
    }
}
