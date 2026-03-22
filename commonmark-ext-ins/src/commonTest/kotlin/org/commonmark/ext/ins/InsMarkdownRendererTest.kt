package org.commonmark.ext.ins

import org.commonmark.Extension
import org.commonmark.parser.Parser
import org.commonmark.renderer.markdown.MarkdownRenderer
import kotlin.test.Test
import kotlin.test.assertEquals

class InsMarkdownRendererTest {
    private val extensions: Set<Extension> = setOf(InsExtension.create())
    private val parser: Parser = Parser.builder().extensions(extensions).build()
    private val renderer: MarkdownRenderer = MarkdownRenderer.builder().extensions(extensions).build()

    @Test
    fun testStrikethrough() {
        assertRoundTrip("++foo++\n")

        assertRoundTrip("\\+\\+foo\\+\\+\n")
    }

    private fun render(source: String): String = renderer.render(parser.parse(source))

    private fun assertRoundTrip(input: String) {
        val rendered = render(input)
        assertEquals(input, rendered)
    }
}
