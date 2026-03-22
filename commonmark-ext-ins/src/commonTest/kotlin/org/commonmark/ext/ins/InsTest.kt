package org.commonmark.ext.ins

import org.commonmark.Extension
import org.commonmark.node.Paragraph
import org.commonmark.node.SourceSpan
import org.commonmark.parser.IncludeSourceSpans
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.renderer.text.TextContentRenderer
import kotlin.test.Test
import kotlin.test.assertEquals

class InsTest {

    private val extensions: Set<Extension> = setOf(InsExtension.create())
    private val parser: Parser = Parser.builder().extensions(extensions).build()
    private val renderer: HtmlRenderer = HtmlRenderer.builder().extensions(extensions).build()
    private val contentRenderer: TextContentRenderer = TextContentRenderer.builder()
        .extensions(extensions).build()

    private fun render(source: String): String {
        return renderer.render(parser.parse(source))
    }

    private fun assertRendering(source: String, expected: String) {
        val actualResult = render(source)
        val expectedFormatted = showTabs("$expected\n\n$source")
        val actualFormatted = showTabs("$actualResult\n\n$source")
        assertEquals(expectedFormatted, actualFormatted)
    }

    @Test
    fun onePlusIsNotEnough() {
        assertRendering("+foo+", "<p>+foo+</p>\n")
    }

    @Test
    fun twoPlusesYay() {
        assertRendering("++foo++", "<p><ins>foo</ins></p>\n")
    }

    @Test
    fun fourPlusesNope() {
        assertRendering("foo ++++", "<p>foo ++++</p>\n")
    }

    @Test
    fun unmatched() {
        assertRendering("++foo", "<p>++foo</p>\n")
        assertRendering("foo++", "<p>foo++</p>\n")
    }

    @Test
    fun threeInnerThree() {
        assertRendering("+++foo+++", "<p>+<ins>foo</ins>+</p>\n")
    }

    @Test
    fun twoInnerThree() {
        assertRendering("++foo+++", "<p><ins>foo</ins>+</p>\n")
    }

    @Test
    fun plusesInside() {
        assertRendering("++foo+bar++", "<p><ins>foo+bar</ins></p>\n")
        assertRendering("++foo++bar++", "<p><ins>foo</ins>bar++</p>\n")
        assertRendering("++foo+++bar++", "<p><ins>foo</ins>+bar++</p>\n")
        assertRendering("++foo++++bar++", "<p><ins>foo</ins><ins>bar</ins></p>\n")
        assertRendering("++foo+++++bar++", "<p><ins>foo</ins>+<ins>bar</ins></p>\n")
        assertRendering("++foo++++++bar++", "<p><ins>foo</ins>++<ins>bar</ins></p>\n")
        assertRendering("++foo+++++++bar++", "<p><ins>foo</ins>+++<ins>bar</ins></p>\n")
    }

    @Test
    fun insWholeParagraphWithOtherDelimiters() {
        assertRendering(
            "++Paragraph with *emphasis* and __strong emphasis__++",
            "<p><ins>Paragraph with <em>emphasis</em> and <strong>strong emphasis</strong></ins></p>\n"
        )
    }

    @Test
    fun insideBlockQuote() {
        assertRendering(
            "> underline ++that++",
            "<blockquote>\n<p>underline <ins>that</ins></p>\n</blockquote>\n"
        )
    }

    @Test
    fun delimited() {
        val document = parser.parse("++foo++")
        val ins = document.firstChild!!.firstChild!! as Ins
        assertEquals("++", ins.openingDelimiter)
        assertEquals("++", ins.closingDelimiter)
    }

    @Test
    fun textContentRenderer() {
        val document = parser.parse("++foo++")
        assertEquals("foo", contentRenderer.render(document))
    }

    @Test
    fun sourceSpans() {
        val sourceSpanParser = Parser.builder()
            .extensions(extensions)
            .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
            .build()

        val document = sourceSpanParser.parse("hey ++there++\n")
        val block = document.firstChild as Paragraph
        val ins = block.lastChild!!
        assertEquals(listOf(SourceSpan.of(0, 4, 4, 9)), ins.getSourceSpans())
    }

    companion object {
        private fun showTabs(s: String): String {
            return s.replace("\t", "\u2192")
        }
    }
}
