package org.commonmark.ext.htmlconverter

import org.commonmark.ext.htmlconverter.internal.HtmlToken
import org.commonmark.ext.htmlconverter.internal.HtmlTokenizer
import org.commonmark.node.*

/**
 * Converts HTML strings to commonmark [Node] AST trees.
 *
 * Supported HTML elements:
 * - Headings: `<h1>` through `<h6>`
 * - Paragraphs: `<p>`
 * - Bold: `<strong>`, `<b>`
 * - Italic: `<em>`, `<i>`
 * - Links: `<a href="...">`
 * - Images: `<img src="..." alt="...">`
 * - Inline code: `<code>` (when not inside `<pre>`)
 * - Code blocks: `<pre><code>` or `<pre>`
 * - Blockquotes: `<blockquote>`
 * - Unordered lists: `<ul>` with `<li>`
 * - Ordered lists: `<ol>` with `<li>`
 * - Thematic breaks: `<hr>`
 * - Line breaks: `<br>`
 * - Divs, spans, sections, articles, main: treated as transparent containers
 *
 * Example usage:
 * ```
 * val markdown = HtmlToMarkdownConverter.convert("<h1>Hello</h1><p>World</p>")
 * // Returns: "# Hello\n\nWorld\n"
 * ```
 */
public object HtmlToMarkdownConverter {
    /**
     * Convert an HTML string to a Markdown string.
     *
     * @param html the HTML to convert
     * @return the resulting Markdown text
     */
    public fun convert(html: String): String {
        val document = convertToDocument(html)
        return renderToMarkdown(document)
    }

    /**
     * Convert an HTML string to a commonmark [Document] node.
     *
     * @param html the HTML to convert
     * @return the root [Document] node of the resulting AST
     */
    public fun convertToDocument(html: String): Document {
        val tokens = HtmlTokenizer(html).tokenize()
        val builder = DocumentBuilder()
        builder.process(tokens)
        return builder.build()
    }

    private fun renderToMarkdown(node: Node): String {
        val sb = StringBuilder()
        val renderer = SimpleMarkdownRenderer(sb)
        renderer.render(node)
        return sb.toString().trimEnd('\n') + "\n"
    }
}

private class DocumentBuilder {
    private val document = Document()
    private val stack = ArrayDeque<NodeContext>()

    init {
        stack.addLast(NodeContext(document, ContextType.BLOCK))
    }

    fun process(tokens: List<HtmlToken>) {
        for (token in tokens) {
            when (token) {
                is HtmlToken.Text -> handleText(token.content)
                is HtmlToken.OpenTag -> handleOpenTag(token)
                is HtmlToken.CloseTag -> handleCloseTag(token)
            }
        }
    }

    fun build(): Document {
        // Finalize any remaining inline text
        flushInlineText()
        return document
    }

    private fun handleText(content: String) {
        val ctx = currentContext()

        if (ctx.type == ContextType.CODE_BLOCK) {
            ctx.textBuffer.append(content)
            return
        }

        if (ctx.type == ContextType.INLINE_CODE) {
            ctx.textBuffer.append(content)
            return
        }

        if (ctx.type == ContextType.BLOCK || ctx.type == ContextType.LIST_ITEM) {
            // If we're in a block context such as document or blockquote,
            // text creates an implicit paragraph
            val trimmed = content.trim()
            if (trimmed.isEmpty()) return

            val paragraph = Paragraph()
            ctx.node.appendChild(paragraph)
            paragraph.appendChild(Text(trimmed))
            return
        }

        // In inline context (paragraph, heading, link, emphasis, etc.)
        if (content.isNotEmpty()) {
            val processed = collapseWhitespace(content)
            if (processed.isNotEmpty()) {
                ctx.node.appendChild(Text(processed))
            }
        }
    }

    private fun handleOpenTag(tag: HtmlToken.OpenTag) {
        when (tag.name) {
            "h1", "h2", "h3", "h4", "h5", "h6" -> {
                flushInlineText()
                val heading = Heading()
                heading.level = tag.name[1].digitToInt()
                appendToCurrentBlock(heading)
                stack.addLast(NodeContext(heading, ContextType.INLINE))
            }

            "p" -> {
                flushInlineText()
                val paragraph = Paragraph()
                appendToCurrentBlock(paragraph)
                stack.addLast(NodeContext(paragraph, ContextType.INLINE))
            }

            "strong", "b" -> {
                val strong = StrongEmphasis()
                strong.delimiter = "**"
                currentContext().node.appendChild(strong)
                stack.addLast(NodeContext(strong, ContextType.INLINE))
            }

            "em", "i" -> {
                val emphasis = Emphasis()
                emphasis.delimiter = "*"
                currentContext().node.appendChild(emphasis)
                stack.addLast(NodeContext(emphasis, ContextType.INLINE))
            }

            "a" -> {
                val link = Link()
                link.destination = tag.attributes["href"] ?: ""
                link.title = tag.attributes["title"]
                currentContext().node.appendChild(link)
                stack.addLast(NodeContext(link, ContextType.INLINE))
            }

            "img" -> {
                val image = Image()
                image.destination = tag.attributes["src"] ?: ""
                image.title = tag.attributes["title"]
                val alt = tag.attributes["alt"] ?: ""
                if (alt.isNotEmpty()) {
                    image.appendChild(Text(alt))
                }
                currentContext().node.appendChild(image)
                // img is self-closing, no stack push needed
            }

            "code" -> {
                val ctx = currentContext()
                if (ctx.type == ContextType.CODE_BLOCK) {
                    // Inside <pre>, just mark that we have a <code> wrapper
                    ctx.hasCodeChild = true
                    return
                }
                // Inline code
                stack.addLast(NodeContext(ctx.node, ContextType.INLINE_CODE))
            }

            "pre" -> {
                flushInlineText()
                val codeBlock = FencedCodeBlock()
                codeBlock.fenceCharacter = "`"
                codeBlock.openingFenceLength = 3
                codeBlock.closingFenceLength = 3

                appendToCurrentBlock(codeBlock)
                stack.addLast(NodeContext(codeBlock, ContextType.CODE_BLOCK))
            }

            "blockquote" -> {
                flushInlineText()
                val blockQuote = BlockQuote()
                appendToCurrentBlock(blockQuote)
                stack.addLast(NodeContext(blockQuote, ContextType.BLOCK))
            }

            "ul" -> {
                flushInlineText()
                val list = BulletList()
                list.marker = "-"
                list.isTight = true
                appendToCurrentBlock(list)
                stack.addLast(NodeContext(list, ContextType.LIST))
            }

            "ol" -> {
                flushInlineText()
                val list = OrderedList()
                list.markerStartNumber = tag.attributes["start"]?.toIntOrNull() ?: 1
                list.markerDelimiter = "."
                list.isTight = true
                appendToCurrentBlock(list)
                stack.addLast(NodeContext(list, ContextType.LIST))
            }

            "li" -> {
                val listItem = ListItem()
                listItem.markerIndent = 0
                listItem.contentIndent = 2
                currentContext().node.appendChild(listItem)
                stack.addLast(NodeContext(listItem, ContextType.LIST_ITEM))
            }

            "hr" -> {
                flushInlineText()
                val tb = ThematicBreak()
                tb.literal = "---"
                appendToCurrentBlock(tb)
            }

            "br" -> {
                val ctx = currentContext()
                if (ctx.type == ContextType.INLINE || ctx.type == ContextType.LIST_ITEM) {
                    ctx.node.appendChild(HardLineBreak())
                }
            }

            // Transparent container elements - push a block context
            "div", "section", "article", "main", "header", "footer", "nav", "aside" -> {
                flushInlineText()
                // Treat as transparent container, don't create a node
                stack.addLast(NodeContext(currentContext().node, ContextType.BLOCK))
            }

            "span" -> {
                // Transparent inline container
                stack.addLast(NodeContext(currentContext().node, ContextType.INLINE))
            }

            else -> {
                // Unknown tag - treat content as inline text pass-through
            }
        }
    }

    private fun handleCloseTag(tag: HtmlToken.CloseTag) {
        when (tag.name) {
            "h1", "h2", "h3", "h4", "h5", "h6", "p",
            "strong", "b", "em", "i", "a",
            "blockquote", "ul", "ol", "li",
            "div", "section", "article", "main", "header", "footer", "nav", "aside",
            "span",
            -> {
                popContext(tag.name)
            }

            "code" -> {
                val ctx = currentContext()
                if (ctx.type == ContextType.CODE_BLOCK) {
                    // Closing </code> inside <pre>, just ignore
                    return
                }
                if (ctx.type == ContextType.INLINE_CODE) {
                    val codeText = ctx.textBuffer.toString()
                    stack.removeLast()
                    val code = Code(codeText)
                    currentContext().node.appendChild(code)
                }
            }

            "pre" -> {
                val ctx = currentContext()
                if (ctx.type == ContextType.CODE_BLOCK) {
                    val codeBlock = ctx.node as FencedCodeBlock
                    var literal = ctx.textBuffer.toString()
                    // Trim a single leading/trailing newline if present
                    if (literal.startsWith("\n")) literal = literal.substring(1)
                    if (literal.endsWith("\n")) literal = literal.substring(0, literal.length - 1)
                    codeBlock.literal = literal + "\n"
                    stack.removeLast()
                }
            }

            else -> {
                // Unknown closing tag - ignore
            }
        }
    }

    private fun currentContext(): NodeContext = stack.last()

    private fun appendToCurrentBlock(node: Node) {
        val ctx = currentContext()
        // If we're in a list item, wrap inline content in a paragraph first
        if (ctx.type == ContextType.LIST_ITEM && node is Block) {
            ctx.node.appendChild(node)
        } else {
            ctx.node.appendChild(node)
        }
    }

    private fun popContext(tagName: String) {
        // Pop matching context
        if (stack.size > 1) {
            val ctx = stack.last()
            // Find the matching tag
            if (matchesTag(ctx, tagName)) {
                flushInlineText()
                stack.removeLast()
                return
            }
            // If no match at top, try to find a match deeper (for malformed HTML)
            for (i in stack.indices.reversed()) {
                if (matchesTag(stack[i], tagName) && i > 0) {
                    while (stack.size > i) {
                        flushInlineText()
                        stack.removeLast()
                    }
                    return
                }
            }
        }
    }

    private fun matchesTag(
        ctx: NodeContext,
        tagName: String,
    ): Boolean =
        when (tagName) {
            "h1", "h2", "h3", "h4", "h5", "h6" -> ctx.node is Heading
            "p" -> ctx.node is Paragraph && ctx.type == ContextType.INLINE
            "strong", "b" -> ctx.node is StrongEmphasis
            "em", "i" -> ctx.node is Emphasis
            "a" -> ctx.node is Link
            "blockquote" -> ctx.node is BlockQuote
            "ul" -> ctx.node is BulletList
            "ol" -> ctx.node is OrderedList
            "li" -> ctx.node is ListItem
            "div", "section", "article", "main", "header", "footer", "nav", "aside" ->
                ctx.type == ContextType.BLOCK && ctx.node !is Document && ctx.node !is BlockQuote && ctx.node !is ListItem

            "span" -> ctx.type == ContextType.INLINE && ctx.node !is Heading && ctx.node !is Paragraph && ctx.node !is Emphasis && ctx.node !is StrongEmphasis && ctx.node !is Link
            else -> false
        }

    private fun flushInlineText() {
        val ctx = currentContext()
        if (ctx.type == ContextType.INLINE_CODE && ctx.textBuffer.isNotEmpty()) {
            val code = Code(ctx.textBuffer.toString())
            ctx.textBuffer.clear()
            stack.removeLast()
            currentContext().node.appendChild(code)
        }
    }

    private fun collapseWhitespace(text: String): String {
        val sb = StringBuilder(text.length)
        var lastWasWhitespace = false
        for (c in text) {
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                if (!lastWasWhitespace) {
                    sb.append(' ')
                    lastWasWhitespace = true
                }
            } else {
                sb.append(c)
                lastWasWhitespace = false
            }
        }
        return sb.toString()
    }
}

private data class NodeContext(
    val node: Node,
    val type: ContextType,
    val textBuffer: StringBuilder = StringBuilder(),
    var hasCodeChild: Boolean = false,
)

private enum class ContextType {
    BLOCK,
    INLINE,
    LIST,
    LIST_ITEM,
    CODE_BLOCK,
    INLINE_CODE,
}

/**
 * Simple markdown renderer that converts the AST back to markdown text.
 * This is used internally by [HtmlToMarkdownConverter.convert].
 */
private class SimpleMarkdownRenderer(
    private val sb: StringBuilder,
) {
    private var lastBlockEnd = false

    fun render(node: Node) {
        when (node) {
            is Document -> renderChildren(node)
            is Heading -> renderHeading(node)
            is Paragraph -> renderParagraph(node)
            is BlockQuote -> renderBlockQuote(node)
            is BulletList -> renderBulletList(node)
            is OrderedList -> renderOrderedList(node)
            is ListItem -> renderChildren(node) // list items rendered by parent list
            is FencedCodeBlock -> renderFencedCodeBlock(node)
            is IndentedCodeBlock -> renderIndentedCodeBlock(node)
            is ThematicBreak -> renderThematicBreak(node)
            is Text -> sb.append(node.literal)
            is Code -> renderCode(node)
            is Emphasis -> renderEmphasis(node)
            is StrongEmphasis -> renderStrongEmphasis(node)
            is Link -> renderLink(node)
            is Image -> renderImage(node)
            is SoftLineBreak -> sb.append('\n')
            is HardLineBreak -> sb.append("  \n")
            is HtmlBlock -> sb.append(node.literal ?: "")
            is HtmlInline -> sb.append(node.literal ?: "")
            else -> renderChildren(node)
        }
    }

    private fun renderHeading(heading: Heading) {
        ensureBlankLine()
        for (i in 0 until heading.level) {
            sb.append('#')
        }
        sb.append(' ')
        renderChildren(heading)
        sb.append('\n')
        lastBlockEnd = true
    }

    private fun renderParagraph(paragraph: Paragraph) {
        ensureBlankLine()
        renderChildren(paragraph)
        sb.append('\n')
        lastBlockEnd = true
    }

    private fun renderBlockQuote(blockQuote: BlockQuote) {
        ensureBlankLine()
        val content = StringBuilder()
        val innerRenderer = SimpleMarkdownRenderer(content)
        innerRenderer.renderChildren(blockQuote)
        val text = content.toString().trimEnd('\n')
        for (line in text.split("\n")) {
            sb.append("> ")
            sb.append(line)
            sb.append('\n')
        }
        lastBlockEnd = true
    }

    private fun renderBulletList(list: BulletList) {
        ensureBlankLine()
        var item = list.firstChild
        while (item != null) {
            if (item is ListItem) {
                renderBulletListItem(item)
            }
            item = item.next
        }
        lastBlockEnd = true
    }

    private fun renderBulletListItem(item: ListItem) {
        sb.append("- ")
        val content = StringBuilder()
        val innerRenderer = SimpleMarkdownRenderer(content)
        innerRenderer.renderChildren(item)
        val text = content.toString().trimEnd('\n')
        val lines = text.split("\n")
        for ((index, line) in lines.withIndex()) {
            if (index > 0) {
                sb.append("  ") // continuation indent
            }
            sb.append(line)
            sb.append('\n')
        }
    }

    private fun renderOrderedList(list: OrderedList) {
        ensureBlankLine()
        var number = list.markerStartNumber ?: 1
        var item = list.firstChild
        while (item != null) {
            if (item is ListItem) {
                renderOrderedListItem(item, number)
                number++
            }
            item = item.next
        }
        lastBlockEnd = true
    }

    private fun renderOrderedListItem(
        item: ListItem,
        number: Int,
    ) {
        sb.append("$number. ")
        val content = StringBuilder()
        val innerRenderer = SimpleMarkdownRenderer(content)
        innerRenderer.renderChildren(item)
        val text = content.toString().trimEnd('\n')
        val lines = text.split("\n")
        val indent = " ".repeat("$number. ".length)
        for ((index, line) in lines.withIndex()) {
            if (index > 0) {
                sb.append(indent)
            }
            sb.append(line)
            sb.append('\n')
        }
    }

    private fun renderFencedCodeBlock(codeBlock: FencedCodeBlock) {
        ensureBlankLine()
        val fence = (codeBlock.fenceCharacter ?: "`").repeat(codeBlock.openingFenceLength ?: 3)
        sb.append(fence)
        val info = codeBlock.info
        if (info != null && info.isNotEmpty()) {
            sb.append(info)
        }
        sb.append('\n')
        sb.append(codeBlock.literal ?: "")
        sb.append(fence)
        sb.append('\n')
        lastBlockEnd = true
    }

    private fun renderIndentedCodeBlock(codeBlock: IndentedCodeBlock) {
        ensureBlankLine()
        val literal = codeBlock.literal ?: ""
        for (line in literal.split("\n")) {
            if (line.isNotEmpty()) {
                sb.append("    ")
                sb.append(line)
            }
            sb.append('\n')
        }
        lastBlockEnd = true
    }

    private fun renderThematicBreak(thematicBreak: ThematicBreak) {
        ensureBlankLine()
        sb.append(thematicBreak.literal ?: "---")
        sb.append('\n')
        lastBlockEnd = true
    }

    private fun renderCode(code: Code) {
        val literal = code.literal
        val backticks =
            if ('`' in literal) {
                "`".repeat(findMaxBacktickRun(literal) + 1)
            } else {
                "`"
            }
        sb.append(backticks)
        if (literal.startsWith("`") || literal.endsWith("`")) {
            sb.append(' ')
            sb.append(literal)
            sb.append(' ')
        } else {
            sb.append(literal)
        }
        sb.append(backticks)
    }

    private fun renderEmphasis(emphasis: Emphasis) {
        sb.append('*')
        renderChildren(emphasis)
        sb.append('*')
    }

    private fun renderStrongEmphasis(strongEmphasis: StrongEmphasis) {
        sb.append("**")
        renderChildren(strongEmphasis)
        sb.append("**")
    }

    private fun renderLink(link: Link) {
        sb.append('[')
        renderChildren(link)
        sb.append("](")
        sb.append(link.destination)
        if (link.title != null) {
            sb.append(" \"")
            sb.append(link.title)
            sb.append('"')
        }
        sb.append(')')
    }

    private fun renderImage(image: Image) {
        sb.append("![")
        renderChildren(image)
        sb.append("](")
        sb.append(image.destination)
        if (image.title != null) {
            sb.append(" \"")
            sb.append(image.title)
            sb.append('"')
        }
        sb.append(')')
    }

    private fun renderChildren(parent: Node) {
        var child = parent.firstChild
        while (child != null) {
            val next = child.next
            render(child)
            child = next
        }
    }

    private fun ensureBlankLine() {
        if (sb.isNotEmpty() && !sb.endsWith("\n\n")) {
            if (sb.endsWith("\n")) {
                sb.append('\n')
            } else {
                sb.append("\n\n")
            }
        }
    }

    private fun findMaxBacktickRun(s: String): Int {
        var max = 0
        var current = 0
        for (c in s) {
            if (c == '`') {
                current++
                if (current > max) max = current
            } else {
                current = 0
            }
        }
        return max
    }
}
