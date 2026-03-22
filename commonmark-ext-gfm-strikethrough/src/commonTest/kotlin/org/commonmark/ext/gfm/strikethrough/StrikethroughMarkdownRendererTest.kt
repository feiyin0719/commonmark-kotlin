package org.commonmark.ext.gfm.strikethrough

import org.commonmark.Extension
import org.commonmark.parser.Parser
import org.commonmark.renderer.markdown.MarkdownRenderer
import kotlin.test.Test
import kotlin.test.assertEquals

class StrikethroughMarkdownRendererTest {

    private val extensions: Set<Extension> = setOf(StrikethroughExtension.create())
    private val parser: Parser = Parser.builder().extensions(extensions).build()
    private val renderer: MarkdownRenderer = MarkdownRenderer.builder().extensions(extensions).build()

    @Test
    fun testStrikethrough() {
        assertRoundTrip("~foo~ ~bar~\n")
        assertRoundTrip("~~foo~~ ~~bar~~\n")
        assertRoundTrip("~~f\\~oo~~ ~~bar~~\n")

        assertRoundTrip("\\~foo\\~\n")
    }

    private fun render(source: String): String {
        return renderer.render(parser.parse(source))
    }

    private fun assertRoundTrip(input: String) {
        val rendered = render(input)
        assertEquals(input, rendered)
    }
}
