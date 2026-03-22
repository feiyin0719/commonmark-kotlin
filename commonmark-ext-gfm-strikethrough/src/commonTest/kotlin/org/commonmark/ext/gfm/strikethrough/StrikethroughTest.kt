package org.commonmark.ext.gfm.strikethrough

import org.commonmark.Extension
import org.commonmark.node.Paragraph
import org.commonmark.node.SourceSpan
import org.commonmark.node.Text
import org.commonmark.parser.IncludeSourceSpans
import org.commonmark.parser.Parser
import org.commonmark.parser.delimiter.DelimiterProcessor
import org.commonmark.parser.delimiter.DelimiterRun
import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.renderer.text.TextContentRenderer
import kotlin.test.Test
import kotlin.test.assertEquals

class StrikethroughTest {
    private val extensions: Set<Extension> = setOf(StrikethroughExtension.create())
    private val parser: Parser = Parser.builder().extensions(extensions).build()
    private val htmlRenderer: HtmlRenderer = HtmlRenderer.builder().extensions(extensions).build()
    private val contentRenderer: TextContentRenderer =
        TextContentRenderer
            .builder()
            .extensions(extensions)
            .build()

    private fun render(source: String): String = htmlRenderer.render(parser.parse(source))

    private fun assertRendering(
        source: String,
        expected: String,
    ) {
        val actualResult = render(source)
        val expectedFormatted = showTabs("$expected\n\n$source")
        val actualFormatted = showTabs("$actualResult\n\n$source")
        assertEquals(expectedFormatted, actualFormatted)
    }

    @Test
    fun oneTildeIsEnough() {
        assertRendering("~foo~", "<p><del>foo</del></p>\n")
    }

    @Test
    fun twoTildesWorksToo() {
        assertRendering("~~foo~~", "<p><del>foo</del></p>\n")
    }

    @Test
    fun fourTildesNope() {
        assertRendering("foo ~~~~", "<p>foo ~~~~</p>\n")
    }

    @Test
    fun unmatched() {
        assertRendering("~~foo", "<p>~~foo</p>\n")
        assertRendering("foo~~", "<p>foo~~</p>\n")
    }

    @Test
    fun threeInnerThree() {
        assertRendering("a ~~~foo~~~", "<p>a ~~~foo~~~</p>\n")
    }

    @Test
    fun twoInnerThree() {
        assertRendering("~~foo~~~", "<p>~~foo~~~</p>\n")
    }

    @Test
    fun tildesInside() {
        assertRendering("~~foo~bar~~", "<p><del>foo~bar</del></p>\n")
        assertRendering("~~foo~~bar~~", "<p><del>foo</del>bar~~</p>\n")
        assertRendering("~~foo~~~bar~~", "<p><del>foo~~~bar</del></p>\n")
        assertRendering("~~foo~~~~bar~~", "<p><del>foo~~~~bar</del></p>\n")
        assertRendering("~~foo~~~~~bar~~", "<p><del>foo~~~~~bar</del></p>\n")
        assertRendering("~~foo~~~~~~bar~~", "<p><del>foo~~~~~~bar</del></p>\n")
    }

    @Test
    fun strikethroughWholeParagraphWithOtherDelimiters() {
        assertRendering(
            "~~Paragraph with *emphasis* and __strong emphasis__~~",
            "<p><del>Paragraph with <em>emphasis</em> and <strong>strong emphasis</strong></del></p>\n",
        )
    }

    @Test
    fun insideBlockQuote() {
        assertRendering(
            "> strike ~~that~~",
            "<blockquote>\n<p>strike <del>that</del></p>\n</blockquote>\n",
        )
    }

    @Test
    fun delimited() {
        val document = parser.parse("~~foo~~")
        val strikethrough = document.firstChild!!.firstChild!! as Strikethrough
        assertEquals("~~", strikethrough.openingDelimiter)
        assertEquals("~~", strikethrough.closingDelimiter)
    }

    @Test
    fun textContentRenderer() {
        val document = parser.parse("~~foo~~")
        assertEquals("/foo/", contentRenderer.render(document))
    }

    @Test
    fun requireTwoTildesOption() {
        val requireTwoTildesParser =
            Parser
                .builder()
                .extensions(
                    setOf(
                        StrikethroughExtension
                            .builder()
                            .requireTwoTildes(true)
                            .build(),
                    ),
                ).customDelimiterProcessor(SubscriptDelimiterProcessor())
                .build()

        val document = requireTwoTildesParser.parse("~foo~ ~~bar~~")
        assertEquals("(sub)foo(/sub) /bar/", contentRenderer.render(document))
    }

    @Test
    fun sourceSpans() {
        val sourceSpanParser =
            Parser
                .builder()
                .extensions(extensions)
                .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
                .build()

        val document = sourceSpanParser.parse("hey ~~there~~\n")
        val block = document.firstChild as Paragraph
        val strikethrough = block.lastChild!!
        assertEquals(listOf(SourceSpan.of(0, 4, 4, 9)), strikethrough.getSourceSpans())
    }

    private class SubscriptDelimiterProcessor : DelimiterProcessor {
        override val openingCharacter: Char get() = '~'
        override val closingCharacter: Char get() = '~'
        override val minLength: Int get() = 1

        override fun process(
            openingRun: DelimiterRun,
            closingRun: DelimiterRun,
        ): Int {
            openingRun.opener.insertAfter(Text("(sub)"))
            closingRun.closer.insertBefore(Text("(/sub)"))
            return 1
        }
    }

    companion object {
        private fun showTabs(s: String): String = s.replace("\t", "\u2192")
    }
}
