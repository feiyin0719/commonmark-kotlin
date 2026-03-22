package org.commonmark.ext.autolink

import org.commonmark.node.*
import org.commonmark.parser.IncludeSourceSpans
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class AutolinkTest {

    private val extensions = setOf(AutolinkExtension.create())
    private val parser = Parser.builder().extensions(extensions).build()
    private val renderer = HtmlRenderer.builder().extensions(extensions).build()

    private val noWwwExtensions = setOf(
        AutolinkExtension.builder()
            .linkTypes(AutolinkType.URL, AutolinkType.EMAIL)
            .build()
    )
    private val noWwwParser = Parser.builder().extensions(noWwwExtensions).build()
    private val noWwwRenderer = HtmlRenderer.builder().extensions(noWwwExtensions).build()

    private fun render(source: String): String {
        return renderer.render(parser.parse(source))
    }

    private fun assertRendering(source: String, expected: String) {
        assertEquals(expected, render(source))
    }

    @Test
    fun oneTextNode() {
        assertRendering(
            "foo http://one.org/ bar http://two.org/",
            "<p>foo <a href=\"http://one.org/\">http://one.org/</a> bar <a href=\"http://two.org/\">http://two.org/</a></p>\n"
        )
    }

    @Test
    fun textNodeAndOthers() {
        assertRendering(
            "foo http://one.org/ bar `code` baz http://two.org/",
            "<p>foo <a href=\"http://one.org/\">http://one.org/</a> bar <code>code</code> baz <a href=\"http://two.org/\">http://two.org/</a></p>\n"
        )
    }

    @Test
    fun tricky() {
        assertRendering(
            "http://example.com/one. Example 2 (see http://example.com/two). Example 3: http://example.com/foo_(bar)",
            "<p><a href=\"http://example.com/one\">http://example.com/one</a>. " +
                    "Example 2 (see <a href=\"http://example.com/two\">http://example.com/two</a>). " +
                    "Example 3: <a href=\"http://example.com/foo_(bar)\">http://example.com/foo_(bar)</a></p>\n"
        )
    }

    @Test
    fun emailUsesMailto() {
        assertRendering(
            "foo@example.com",
            "<p><a href=\"mailto:foo@example.com\">foo@example.com</a></p>\n"
        )
    }

    @Test
    fun emailWithTldNotLinked() {
        assertRendering("foo@com", "<p>foo@com</p>\n")
    }

    @Test
    fun dontLinkTextWithinLinks() {
        assertRendering(
            "<http://example.com>",
            "<p><a href=\"http://example.com\">http://example.com</a></p>\n"
        )
    }

    @Test
    fun wwwLinks() {
        assertRendering(
            "www.example.com",
            "<p><a href=\"http://www.example.com\">www.example.com</a></p>\n"
        )
    }

    @Test
    fun noWwwLinks() {
        val html = noWwwRenderer.render(noWwwParser.parse("www.example.com"))
        assertEquals("<p>www.example.com</p>\n", html)
    }

    @Test
    fun sourceSpans() {
        val parser = Parser.builder()
            .extensions(extensions)
            .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
            .build()
        val document = parser.parse(
            "abc\n" +
                    "http://example.com/one\n" +
                    "def http://example.com/two\n" +
                    "ghi http://example.com/three jkl"
        )

        val paragraph = document.firstChild as Paragraph
        val abc = paragraph.firstChild as Text
        assertEquals(listOf(SourceSpan.of(0, 0, 0, 3)), abc.getSourceSpans())

        assertIs<SoftLineBreak>(abc.next)

        val one = abc.next!!.next as Link
        assertEquals("http://example.com/one", one.destination)
        assertEquals(listOf(SourceSpan.of(1, 0, 4, 22)), one.getSourceSpans())

        assertIs<SoftLineBreak>(one.next)

        val def = one.next!!.next as Text
        assertEquals("def ", def.literal)
        assertEquals(listOf(SourceSpan.of(2, 0, 27, 4)), def.getSourceSpans())

        val two = def.next as Link
        assertEquals("http://example.com/two", two.destination)
        assertEquals(listOf(SourceSpan.of(2, 4, 31, 22)), two.getSourceSpans())

        assertIs<SoftLineBreak>(two.next)

        val ghi = two.next!!.next as Text
        assertEquals("ghi ", ghi.literal)
        assertEquals(listOf(SourceSpan.of(3, 0, 54, 4)), ghi.getSourceSpans())

        val three = ghi.next as Link
        assertEquals("http://example.com/three", three.destination)
        assertEquals(listOf(SourceSpan.of(3, 4, 58, 24)), three.getSourceSpans())

        val jkl = three.next as Text
        assertEquals(" jkl", jkl.literal)
        assertEquals(listOf(SourceSpan.of(3, 28, 82, 4)), jkl.getSourceSpans())
    }
}
