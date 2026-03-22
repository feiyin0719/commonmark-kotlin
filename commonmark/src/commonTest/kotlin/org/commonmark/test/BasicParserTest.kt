package org.commonmark.test

import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Basic smoke test to verify parsing and rendering works.
 */
class BasicParserTest {
    private val parser = Parser.builder().build()
    private val renderer = HtmlRenderer.builder().build()

    private fun render(source: String): String {
        val node = parser.parse(source)
        return renderer.render(node)
    }

    @Test
    fun testParagraph() {
        assertEquals("<p>hello</p>\n", render("hello"))
    }

    @Test
    fun testHeading() {
        assertEquals("<h1>Hello</h1>\n", render("# Hello"))
    }

    @Test
    fun testEmphasis() {
        assertEquals("<p><em>hello</em></p>\n", render("*hello*"))
    }

    @Test
    fun testStrongEmphasis() {
        assertEquals("<p><strong>hello</strong></p>\n", render("**hello**"))
    }

    @Test
    fun testInlineCode() {
        assertEquals("<p><code>hello</code></p>\n", render("`hello`"))
    }

    @Test
    fun testLink() {
        assertEquals("<p><a href=\"/url\">text</a></p>\n", render("[text](/url)"))
    }

    @Test
    fun testImage() {
        assertEquals("<p><img src=\"/url\" alt=\"text\" /></p>\n", render("![text](/url)"))
    }

    @Test
    fun testBlockQuote() {
        assertEquals("<blockquote>\n<p>hello</p>\n</blockquote>\n", render("> hello"))
    }

    @Test
    fun testBulletList() {
        assertEquals("<ul>\n<li>one</li>\n<li>two</li>\n</ul>\n", render("- one\n- two"))
    }

    @Test
    fun testOrderedList() {
        assertEquals("<ol>\n<li>one</li>\n<li>two</li>\n</ol>\n", render("1. one\n2. two"))
    }

    @Test
    fun testFencedCodeBlock() {
        assertEquals("<pre><code>hello\n</code></pre>\n", render("```\nhello\n```"))
    }

    @Test
    fun testIndentedCodeBlock() {
        assertEquals("<pre><code>hello\n</code></pre>\n", render("    hello"))
    }

    @Test
    fun testThematicBreak() {
        assertEquals("<hr />\n", render("---"))
    }

    @Test
    fun testHtmlBlock() {
        assertEquals("<div>\nhello\n</div>\n", render("<div>\nhello\n</div>"))
    }

    @Test
    fun testHardLineBreak() {
        assertEquals("<p>hello<br />\nworld</p>\n", render("hello  \nworld"))
    }

    @Test
    fun testSoftLineBreak() {
        assertEquals("<p>hello\nworld</p>\n", render("hello\nworld"))
    }

    @Test
    fun testSeTextHeading() {
        assertEquals("<h1>Hello</h1>\n", render("Hello\n====="))
    }

    @Test
    fun testLinkReference() {
        assertEquals("<p><a href=\"/url\">text</a></p>\n", render("[text][label]\n\n[label]: /url"))
    }

    @Test
    fun testMultipleBlocks() {
        val source = "# Title\n\nParagraph with *emphasis*.\n\n> Quote\n\n- item 1\n- item 2"
        val expected = "<h1>Title</h1>\n<p>Paragraph with <em>emphasis</em>.</p>\n<blockquote>\n<p>Quote</p>\n</blockquote>\n<ul>\n<li>item 1</li>\n<li>item 2</li>\n</ul>\n"
        assertEquals(expected, render(source))
    }
}
