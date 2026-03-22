package org.commonmark.ext.footnotes

import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.markdown.MarkdownRenderer
import kotlin.test.Test
import kotlin.test.assertEquals

class FootnoteMarkdownRendererTest {

    private val extensions = setOf(FootnotesExtension.builder().inlineFootnotes(true).build())
    private val parser = Parser.builder().extensions(extensions).build()
    private val renderer = MarkdownRenderer.builder().extensions(extensions).build()

    @Test
    fun testSimple() {
        assertRoundTrip("Test [^foo]\n\n[^foo]: note\n")
    }

    @Test
    fun testUnreferenced() {
        // Whether a reference has a corresponding definition or vice versa shouldn't matter for Markdown rendering.
        assertRoundTrip("Test [^foo]\n\n[^foo]: one\n\n[^bar]: two\n")
    }

    @Test
    fun testFootnoteWithBlock() {
        assertRoundTrip("Test [^foo]\n\n[^foo]: - foo\n    - bar\n")
    }

    @Test
    fun testBackslashInLabel() {
        assertRoundTrip("[^\\foo]\n\n[^\\foo]: note\n")
    }

    @Test
    fun testMultipleLines() {
        assertRoundTrip("Test [^1]\n\n[^1]: footnote l1\n    footnote l2\n")
    }

    @Test
    fun testMultipleParagraphs() {
        // Note that the line between p1 and p2 could be blank too (instead of 4 spaces), but we currently don't
        // preserve that information.
        assertRoundTrip("Test [^1]\n\n[^1]: footnote p1\n    \n    footnote p2\n")
    }

    @Test
    fun testInline() {
        assertRoundTrip("^[test *foo*]\n")
    }

    private fun assertRoundTrip(input: String) {
        val rendered = parseAndRender(input)
        assertEquals(input, rendered)
    }

    private fun parseAndRender(source: String): String {
        val parsed: Node = parser.parse(source)
        return renderer.render(parsed)
    }
}
