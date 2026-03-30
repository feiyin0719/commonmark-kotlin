package org.commonmark.ext.htmlconverter

import org.commonmark.node.*
import org.commonmark.parser.Parser
import org.commonmark.renderer.markdown.MarkdownRenderer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class HtmlToMarkdownConverterTest {
    @Test
    fun convertHeadings() {
        assertEquals("# Hello\n", HtmlToMarkdownConverter.convert("<h1>Hello</h1>"))
        assertEquals("## World\n", HtmlToMarkdownConverter.convert("<h2>World</h2>"))
        assertEquals("### Level 3\n", HtmlToMarkdownConverter.convert("<h3>Level 3</h3>"))
        assertEquals("#### Level 4\n", HtmlToMarkdownConverter.convert("<h4>Level 4</h4>"))
        assertEquals("##### Level 5\n", HtmlToMarkdownConverter.convert("<h5>Level 5</h5>"))
        assertEquals("###### Level 6\n", HtmlToMarkdownConverter.convert("<h6>Level 6</h6>"))
    }

    @Test
    fun convertParagraphs() {
        assertEquals("Hello World\n", HtmlToMarkdownConverter.convert("<p>Hello World</p>"))
    }

    @Test
    fun convertMultipleParagraphs() {
        assertEquals(
            "First\n\nSecond\n",
            HtmlToMarkdownConverter.convert("<p>First</p><p>Second</p>"),
        )
    }

    @Test
    fun convertBold() {
        assertEquals(
            "Hello **World**\n",
            HtmlToMarkdownConverter.convert("<p>Hello <strong>World</strong></p>"),
        )
        assertEquals(
            "Hello **World**\n",
            HtmlToMarkdownConverter.convert("<p>Hello <b>World</b></p>"),
        )
    }

    @Test
    fun convertItalic() {
        assertEquals(
            "Hello *World*\n",
            HtmlToMarkdownConverter.convert("<p>Hello <em>World</em></p>"),
        )
        assertEquals(
            "Hello *World*\n",
            HtmlToMarkdownConverter.convert("<p>Hello <i>World</i></p>"),
        )
    }

    @Test
    fun convertBoldItalic() {
        assertEquals(
            "Hello ***World***\n",
            HtmlToMarkdownConverter.convert("<p>Hello <strong><em>World</em></strong></p>"),
        )
    }

    @Test
    fun convertLink() {
        assertEquals(
            "[Google](https://google.com)\n",
            HtmlToMarkdownConverter.convert("<p><a href=\"https://google.com\">Google</a></p>"),
        )
    }

    @Test
    fun convertLinkWithTitle() {
        assertEquals(
            "[Google](https://google.com \"Search\")\n",
            HtmlToMarkdownConverter.convert("<p><a href=\"https://google.com\" title=\"Search\">Google</a></p>"),
        )
    }

    @Test
    fun convertImage() {
        assertEquals(
            "![Alt text](https://example.com/image.png)\n",
            HtmlToMarkdownConverter.convert("<p><img src=\"https://example.com/image.png\" alt=\"Alt text\"></p>"),
        )
    }

    @Test
    fun convertImageWithTitle() {
        assertEquals(
            "![Alt](https://example.com/img.png \"Title\")\n",
            HtmlToMarkdownConverter.convert("<p><img src=\"https://example.com/img.png\" alt=\"Alt\" title=\"Title\"></p>"),
        )
    }

    @Test
    fun convertInlineCode() {
        assertEquals(
            "Use `println()` to print\n",
            HtmlToMarkdownConverter.convert("<p>Use <code>println()</code> to print</p>"),
        )
    }

    @Test
    fun convertCodeBlock() {
        assertEquals(
            "```\nval x = 1\n```\n",
            HtmlToMarkdownConverter.convert("<pre><code>val x = 1</code></pre>"),
        )
    }

    @Test
    fun convertBlockquote() {
        assertEquals(
            "> Hello World\n",
            HtmlToMarkdownConverter.convert("<blockquote><p>Hello World</p></blockquote>"),
        )
    }

    @Test
    fun convertUnorderedList() {
        assertEquals(
            "- Item 1\n- Item 2\n- Item 3\n",
            HtmlToMarkdownConverter.convert("<ul><li>Item 1</li><li>Item 2</li><li>Item 3</li></ul>"),
        )
    }

    @Test
    fun convertOrderedList() {
        assertEquals(
            "1. First\n2. Second\n3. Third\n",
            HtmlToMarkdownConverter.convert("<ol><li>First</li><li>Second</li><li>Third</li></ol>"),
        )
    }

    @Test
    fun convertOrderedListWithStart() {
        assertEquals(
            "5. Fifth\n6. Sixth\n",
            HtmlToMarkdownConverter.convert("<ol start=\"5\"><li>Fifth</li><li>Sixth</li></ol>"),
        )
    }

    @Test
    fun convertHorizontalRule() {
        assertEquals(
            "First\n\n---\n\nSecond\n",
            HtmlToMarkdownConverter.convert("<p>First</p><hr><p>Second</p>"),
        )
    }

    @Test
    fun convertLineBreak() {
        assertEquals(
            "Line 1  \nLine 2\n",
            HtmlToMarkdownConverter.convert("<p>Line 1<br>Line 2</p>"),
        )
    }

    @Test
    fun convertComplexDocument() {
        val html = """
            <h1>Title</h1>
            <p>A paragraph with <strong>bold</strong> and <em>italic</em> text.</p>
            <ul>
                <li>Item 1</li>
                <li>Item 2</li>
            </ul>
            <blockquote>
                <p>A quote</p>
            </blockquote>
        """.trimIndent()
        val result = HtmlToMarkdownConverter.convert(html)
        // Verify it contains the key elements
        assertContains(result, "# Title")
        assertContains(result, "**bold**")
        assertContains(result, "*italic*")
        assertContains(result, "- Item 1")
        assertContains(result, "- Item 2")
        assertContains(result, "> A quote")
    }

    @Test
    fun convertToDocumentProducesValidAST() {
        val doc = HtmlToMarkdownConverter.convertToDocument("<h1>Hello</h1><p>World</p>")
        assertIs<Document>(doc)

        val heading = doc.firstChild
        assertNotNull(heading)
        assertIs<Heading>(heading)
        assertEquals(1, heading.level)

        val paragraph = heading.next
        assertNotNull(paragraph)
        assertIs<Paragraph>(paragraph)
    }

    @Test
    fun convertHtmlEntities() {
        assertEquals(
            "A & B < C > D\n",
            HtmlToMarkdownConverter.convert("<p>A &amp; B &lt; C &gt; D</p>"),
        )
    }

    @Test
    fun convertNestedLists() {
        val html = "<ul><li>A<ul><li>A1</li><li>A2</li></ul></li><li>B</li></ul>"
        val result = HtmlToMarkdownConverter.convert(html)
        assertContains(result, "- A")
        assertContains(result, "- B")
    }

    @Test
    fun convertDivAsTransparentContainer() {
        assertEquals(
            "Hello\n",
            HtmlToMarkdownConverter.convert("<div><p>Hello</p></div>"),
        )
    }

    @Test
    fun convertEmptyHtml() {
        assertEquals("\n", HtmlToMarkdownConverter.convert(""))
    }

    @Test
    fun convertWhitespaceOnly() {
        assertEquals("\n", HtmlToMarkdownConverter.convert("   \n  \t  "))
    }

    @Test
    fun convertPreWithLanguage() {
        // Pre blocks don't carry language info from class, but the converter handles <pre><code>
        val result = HtmlToMarkdownConverter.convert("<pre><code>hello()</code></pre>")
        assertContains(result, "```")
        assertContains(result, "hello()")
    }

    private fun assertContains(
        text: String,
        substring: String,
    ) {
        if (substring !in text) {
            throw AssertionError("Expected \"$text\" to contain \"$substring\"")
        }
    }
}

class HtmlConverterExtensionTest {
    @Test
    fun extensionWithMarkdownRenderer() {
        val extensions = listOf(HtmlConverterExtension.create())
        val parser = Parser.builder().build()
        val renderer = MarkdownRenderer.builder().extensions(extensions).build()

        // Markdown containing an HTML block
        val input = "<div>\n<h1>Hello</h1>\n</div>\n"
        val doc = parser.parse(input)
        val output = renderer.render(doc)

        // The HTML block should be converted to markdown equivalents
        // rather than passed through as raw HTML
        assertNotNull(output)
    }

    @Test
    fun extensionCreation() {
        val extension = HtmlConverterExtension.create()
        assertNotNull(extension)
    }
}
