package org.commonmark.ext.heading.anchor

import org.commonmark.Extension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import kotlin.test.Test
import kotlin.test.assertEquals

class HeadingAnchorTest {

    private val extensions: Set<Extension> = setOf(HeadingAnchorExtension.create())
    private val parser: Parser = Parser.builder().build()
    private val renderer: HtmlRenderer = HtmlRenderer.builder().extensions(extensions).build()

    private fun render(source: String): String {
        return renderer.render(parser.parse(source))
    }

    private fun assertRendering(source: String, expected: String) {
        val actual = render(source)
        val expectedWithSource = showTabs("$expected\n\n$source")
        val actualWithSource = showTabs("$actual\n\n$source")
        assertEquals(expectedWithSource, actualWithSource)
    }

    private fun showTabs(s: String): String {
        return s.replace("\t", "\u2192")
    }

    @Test
    fun baseCaseSingleHeader() {
        assertRendering(
            "# Heading here\n",
            "<h1 id=\"heading-here\">Heading here</h1>\n"
        )
    }

    @Test
    fun singleHeaderWithCodeBlock() {
        assertRendering(
            "Hi there\n# Heading `here`\n",
            "<p>Hi there</p>\n<h1 id=\"heading-here\">Heading <code>here</code></h1>\n"
        )
    }

    @Test
    fun duplicateHeadersMakeUniqueIds() {
        assertRendering(
            "# Heading here\n# Heading here",
            "<h1 id=\"heading-here\">Heading here</h1>\n<h1 id=\"heading-here-1\">Heading here</h1>\n"
        )
    }

    @Test
    fun testSupplementalDiacriticalMarks() {
        assertRendering("# a\u1DC0", "<h1 id=\"a\u1DC0\">a\u1DC0</h1>\n")
    }

    @Test
    fun testUndertieUnicodeDisplayed() {
        assertRendering("# undertie \u203F", "<h1 id=\"undertie-\u203F\">undertie \u203F</h1>\n")
    }

    @Test
    fun testExplicitHeaderCollision() {
        assertRendering(
            "# Header\n# Header\n# Header-1",
            "<h1 id=\"header\">Header</h1>\n" +
                    "<h1 id=\"header-1\">Header</h1>\n" +
                    "<h1 id=\"header-1\">Header-1</h1>\n"
        )
    }

    @Test
    fun testCaseIsIgnoredWhenComparingIds() {
        assertRendering(
            "# HEADING here\n" +
                    "# heading here",
            "<h1 id=\"heading-here\">HEADING here</h1>\n" +
                    "<h1 id=\"heading-here-1\">heading here</h1>\n"
        )
    }

    @Test
    fun testNestedBlocks() {
        assertRendering(
            "## `h` `e` **l** *l* o",
            "<h2 id=\"h-e-l-l-o\"><code>h</code> <code>e</code> <strong>l</strong> <em>l</em> o</h2>\n"
        )
    }

    @Test
    fun boldEmphasisCharacters() {
        assertRendering(
            "# _hello_ **there**",
            "<h1 id=\"hello-there\"><em>hello</em> <strong>there</strong></h1>\n"
        )
    }

    @Test
    fun testStrongEmphasis() {
        assertRendering(
            "# _**Hi there**_",
            "<h1 id=\"hi-there\"><em><strong>Hi there</strong></em></h1>\n"
        )
    }

    @Test
    fun testMultipleSpacesKept() {
        assertRendering("# Hi  There", "<h1 id=\"hi--there\">Hi  There</h1>\n")
    }

    @Test
    fun testNonAsciiCharacterHeading() {
        assertRendering("# b\u00e4r", "<h1 id=\"b\u00e4r\">b\u00e4r</h1>\n")
    }

    @Test
    fun testCombiningDiaeresis() {
        assertRendering("# Product\u036D\u036B", "<h1 id=\"product\u036D\u036B\">Product\u036D\u036B</h1>\n")
    }
}
