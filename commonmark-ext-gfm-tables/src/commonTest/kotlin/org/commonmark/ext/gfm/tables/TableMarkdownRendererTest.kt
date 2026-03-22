package org.commonmark.ext.gfm.tables

import org.commonmark.Extension
import org.commonmark.parser.Parser
import org.commonmark.renderer.markdown.MarkdownRenderer
import kotlin.test.Test
import kotlin.test.assertEquals

class TableMarkdownRendererTest {

    private val extensions: Set<Extension> = setOf(TablesExtension.create())
    private val parser: Parser = Parser.builder().extensions(extensions).build()
    private val renderer: MarkdownRenderer = MarkdownRenderer.builder().extensions(extensions).build()

    private fun render(source: String): String {
        return renderer.render(parser.parse(source))
    }

    private fun assertRoundTrip(input: String) {
        val rendered = render(input)
        assertEquals(input, rendered)
    }

    @Test
    fun testHeadNoBody() {
        assertRoundTrip("|Abc|\n|---|\n")
        assertRoundTrip("|Abc|Def|\n|---|---|\n")
        assertRoundTrip("|Abc||\n|---|---|\n")
    }

    @Test
    fun testHeadAndBody() {
        assertRoundTrip("|Abc|\n|---|\n|1|\n")
        assertRoundTrip("|Abc|Def|\n|---|---|\n|1|2|\n")
    }

    @Test
    fun testBodyHasFewerColumns() {
        // Could try not to write empty trailing cells but this is fine too
        assertRoundTrip("|Abc|Def|\n|---|---|\n|1||\n")
    }

    @Test
    fun testAlignment() {
        assertRoundTrip("|Abc|Def|\n|:---|---|\n|1|2|\n")
        assertRoundTrip("|Abc|Def|\n|---|---:|\n|1|2|\n")
        assertRoundTrip("|Abc|Def|\n|:---:|:---:|\n|1|2|\n")
    }

    @Test
    fun testInsideBlockQuote() {
        assertRoundTrip("> |Abc|Def|\n> |---|---|\n> |1|2|\n")
    }

    @Test
    fun testMultipleTables() {
        assertRoundTrip("|Abc|Def|\n|---|---|\n\n|One|\n|---|\n|Only|\n")
    }

    @Test
    fun testEscaping() {
        assertRoundTrip("|Abc|Def|\n|---|---|\n|Pipe in|text \\||\n")
        assertRoundTrip("|Abc|Def|\n|---|---|\n|Pipe in|code `\\|`|\n")
        assertRoundTrip("|Abc|Def|\n|---|---|\n|Inline HTML|<span>Foo\\|bar</span>|\n")
    }

    @Test
    fun testEscaped() {
        // `|` in Text nodes needs to be escaped, otherwise the generated Markdown does not get parsed back as a table
        assertRoundTrip("\\|Abc\\|\n\\|---\\|\n")
    }
}
