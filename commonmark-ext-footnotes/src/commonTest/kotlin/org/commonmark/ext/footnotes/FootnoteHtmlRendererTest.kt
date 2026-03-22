package org.commonmark.ext.footnotes

import org.commonmark.node.Document
import org.commonmark.node.Paragraph
import org.commonmark.node.Text
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import kotlin.test.Test
import kotlin.test.assertEquals

class FootnoteHtmlRendererTest {

    private val extensions = setOf(FootnotesExtension.create())
    private val parser = Parser.builder().extensions(extensions).build()
    private val renderer = HtmlRenderer.builder().extensions(extensions).build()

    private fun render(source: String): String {
        return renderer.render(parser.parse(source))
    }

    private fun assertRendering(source: String, expected: String) {
        assertEquals(expected, render(source))
    }

    @Test
    fun testOne() {
        assertRendering(
            "Test [^foo]\n\n[^foo]: note\n",
            "<p>Test <sup class=\"footnote-ref\"><a href=\"#fn-foo\" id=\"fnref-foo\" data-footnote-ref>1</a></sup></p>\n" +
                    "<section class=\"footnotes\" data-footnotes>\n" +
                    "<ol>\n" +
                    "<li id=\"fn-foo\">\n" +
                    "<p>note <a href=\"#fnref-foo\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"1\" aria-label=\"Back to reference 1\">\u21A9</a></p>\n" +
                    "</li>\n" +
                    "</ol>\n" +
                    "</section>\n"
        )
    }

    @Test
    fun testLabelNormalization() {
        // Labels match via their normalized form. For the href and IDs to match, rendering needs to use the
        // label from the definition consistently.
        assertRendering(
            "Test [^bar]\n\n[^BAR]: note\n",
            "<p>Test <sup class=\"footnote-ref\"><a href=\"#fn-BAR\" id=\"fnref-BAR\" data-footnote-ref>1</a></sup></p>\n" +
                    "<section class=\"footnotes\" data-footnotes>\n" +
                    "<ol>\n" +
                    "<li id=\"fn-BAR\">\n" +
                    "<p>note <a href=\"#fnref-BAR\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"1\" aria-label=\"Back to reference 1\">\u21A9</a></p>\n" +
                    "</li>\n" +
                    "</ol>\n" +
                    "</section>\n"
        )
    }

    @Test
    fun testMultipleReferences() {
        // Tests a few things:
        // - Numbering is based on the reference order, not the definition order
        // - The same number is used when a definition is referenced multiple times
        // - Multiple backrefs are rendered
        assertRendering(
            "First [^foo]\n\nThen [^bar]\n\nThen [^foo] again\n\n[^bar]: b\n[^foo]: f\n",
            "<p>First <sup class=\"footnote-ref\"><a href=\"#fn-foo\" id=\"fnref-foo\" data-footnote-ref>1</a></sup></p>\n" +
                    "<p>Then <sup class=\"footnote-ref\"><a href=\"#fn-bar\" id=\"fnref-bar\" data-footnote-ref>2</a></sup></p>\n" +
                    "<p>Then <sup class=\"footnote-ref\"><a href=\"#fn-foo\" id=\"fnref-foo-2\" data-footnote-ref>1</a></sup> again</p>\n" +
                    "<section class=\"footnotes\" data-footnotes>\n" +
                    "<ol>\n" +
                    "<li id=\"fn-foo\">\n" +
                    "<p>f <a href=\"#fnref-foo\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"1\" aria-label=\"Back to reference 1\">\u21A9</a> <a href=\"#fnref-foo-2\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"1-2\" aria-label=\"Back to reference 1-2\"><sup class=\"footnote-ref\">2</sup>\u21A9</a></p>\n" +
                    "</li>\n" +
                    "<li id=\"fn-bar\">\n" +
                    "<p>b <a href=\"#fnref-bar\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"2\" aria-label=\"Back to reference 2\">\u21A9</a></p>\n" +
                    "</li>\n" +
                    "</ol>\n" +
                    "</section>\n"
        )
    }

    @Test
    fun testDefinitionWithTwoParagraphs() {
        // With two paragraphs, the backref <a> should be added to the second one
        assertRendering(
            "Test [^foo]\n\n[^foo]: one\n    \n    two\n",
            "<p>Test <sup class=\"footnote-ref\"><a href=\"#fn-foo\" id=\"fnref-foo\" data-footnote-ref>1</a></sup></p>\n" +
                    "<section class=\"footnotes\" data-footnotes>\n" +
                    "<ol>\n" +
                    "<li id=\"fn-foo\">\n" +
                    "<p>one</p>\n" +
                    "<p>two <a href=\"#fnref-foo\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"1\" aria-label=\"Back to reference 1\">\u21A9</a></p>\n" +
                    "</li>\n" +
                    "</ol>\n" +
                    "</section>\n"
        )
    }

    @Test
    fun testDefinitionWithList() {
        assertRendering(
            "Test [^foo]\n\n[^foo]:\n    - one\n    - two\n",
            "<p>Test <sup class=\"footnote-ref\"><a href=\"#fn-foo\" id=\"fnref-foo\" data-footnote-ref>1</a></sup></p>\n" +
                    "<section class=\"footnotes\" data-footnotes>\n" +
                    "<ol>\n" +
                    "<li id=\"fn-foo\">\n" +
                    "<ul>\n" +
                    "<li>one</li>\n" +
                    "<li>two</li>\n" +
                    "</ul>\n" +
                    "<a href=\"#fnref-foo\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"1\" aria-label=\"Back to reference 1\">\u21A9</a></li>\n" +
                    "</ol>\n" +
                    "</section>\n"
        )
    }

    // See docs on FootnoteHtmlNodeRenderer about nested footnotes.

    @Test
    fun testNestedFootnotesSimple() {
        assertRendering(
            "[^foo1]\n" +
                    "\n" +
                    "[^foo1]: one [^foo2]\n" +
                    "[^foo2]: two\n",
            "<p><sup class=\"footnote-ref\"><a href=\"#fn-foo1\" id=\"fnref-foo1\" data-footnote-ref>1</a></sup></p>\n" +
                    "<section class=\"footnotes\" data-footnotes>\n" +
                    "<ol>\n" +
                    "<li id=\"fn-foo1\">\n" +
                    "<p>one <sup class=\"footnote-ref\"><a href=\"#fn-foo2\" id=\"fnref-foo2\" data-footnote-ref>2</a></sup> <a href=\"#fnref-foo1\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"1\" aria-label=\"Back to reference 1\">\u21A9</a></p>\n" +
                    "</li>\n" +
                    "<li id=\"fn-foo2\">\n" +
                    "<p>two <a href=\"#fnref-foo2\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"2\" aria-label=\"Back to reference 2\">\u21A9</a></p>\n" +
                    "</li>\n" +
                    "</ol>\n" +
                    "</section>\n"
        )
    }

    @Test
    fun testNestedFootnotesOrder() {
        // GitHub has a strange result here, the definitions are in order: 1. bar, 2. foo.
        // The reason is that the number is done based on all references in document order, including references in
        // definitions. So [^bar] from the first line is first.
        assertRendering(
            "[^foo]: foo [^bar]\n" +
                    "\n" +
                    "[^foo]\n" +
                    "\n" +
                    "[^bar]: bar\n",
            "<p><sup class=\"footnote-ref\"><a href=\"#fn-foo\" id=\"fnref-foo\" data-footnote-ref>1</a></sup></p>\n" +
                    "<section class=\"footnotes\" data-footnotes>\n" +
                    "<ol>\n" +
                    "<li id=\"fn-foo\">\n" +
                    "<p>foo <sup class=\"footnote-ref\"><a href=\"#fn-bar\" id=\"fnref-bar\" data-footnote-ref>2</a></sup> <a href=\"#fnref-foo\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"1\" aria-label=\"Back to reference 1\">\u21A9</a></p>\n" +
                    "</li>\n" +
                    "<li id=\"fn-bar\">\n" +
                    "<p>bar <a href=\"#fnref-bar\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"2\" aria-label=\"Back to reference 2\">\u21A9</a></p>\n" +
                    "</li>\n" +
                    "</ol>\n" +
                    "</section>\n"
        )
    }

    @Test
    fun testNestedFootnotesOrder2() {
        assertRendering(
            "[^1]\n" +
                    "\n" +
                    "[^4]: four\n" +
                    "[^3]: three [^4]\n" +
                    "[^2]: two [^4]\n" +
                    "[^1]: one [^2][^3]\n",
            "<p><sup class=\"footnote-ref\"><a href=\"#fn-1\" id=\"fnref-1\" data-footnote-ref>1</a></sup></p>\n" +
                    "<section class=\"footnotes\" data-footnotes>\n" +
                    "<ol>\n" +
                    "<li id=\"fn-1\">\n" +
                    "<p>one <sup class=\"footnote-ref\"><a href=\"#fn-2\" id=\"fnref-2\" data-footnote-ref>2</a></sup><sup class=\"footnote-ref\"><a href=\"#fn-3\" id=\"fnref-3\" data-footnote-ref>3</a></sup> <a href=\"#fnref-1\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"1\" aria-label=\"Back to reference 1\">\u21A9</a></p>\n" +
                    "</li>\n" +
                    "<li id=\"fn-2\">\n" +
                    "<p>two <sup class=\"footnote-ref\"><a href=\"#fn-4\" id=\"fnref-4\" data-footnote-ref>4</a></sup> <a href=\"#fnref-2\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"2\" aria-label=\"Back to reference 2\">\u21A9</a></p>\n" +
                    "</li>\n" +
                    "<li id=\"fn-3\">\n" +
                    "<p>three <sup class=\"footnote-ref\"><a href=\"#fn-4\" id=\"fnref-4-2\" data-footnote-ref>4</a></sup> <a href=\"#fnref-3\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"3\" aria-label=\"Back to reference 3\">\u21A9</a></p>\n" +
                    "</li>\n" +
                    "<li id=\"fn-4\">\n" +
                    "<p>four <a href=\"#fnref-4\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"4\" aria-label=\"Back to reference 4\">\u21A9</a> <a href=\"#fnref-4-2\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"4-2\" aria-label=\"Back to reference 4-2\"><sup class=\"footnote-ref\">2</sup>\u21A9</a></p>\n" +
                    "</li>\n" +
                    "</ol>\n" +
                    "</section>\n"
        )
    }

    @Test
    fun testNestedFootnotesCycle() {
        // Footnotes can contain cycles, lol.
        assertRendering(
            "[^foo1]\n" +
                    "\n" +
                    "[^foo1]: one [^foo2]\n" +
                    "[^foo2]: two [^foo1]\n",
            "<p><sup class=\"footnote-ref\"><a href=\"#fn-foo1\" id=\"fnref-foo1\" data-footnote-ref>1</a></sup></p>\n" +
                    "<section class=\"footnotes\" data-footnotes>\n" +
                    "<ol>\n" +
                    "<li id=\"fn-foo1\">\n" +
                    "<p>one <sup class=\"footnote-ref\"><a href=\"#fn-foo2\" id=\"fnref-foo2\" data-footnote-ref>2</a></sup> <a href=\"#fnref-foo1\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"1\" aria-label=\"Back to reference 1\">\u21A9</a> <a href=\"#fnref-foo1-2\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"1-2\" aria-label=\"Back to reference 1-2\"><sup class=\"footnote-ref\">2</sup>\u21A9</a></p>\n" +
                    "</li>\n" +
                    "<li id=\"fn-foo2\">\n" +
                    "<p>two <sup class=\"footnote-ref\"><a href=\"#fn-foo1\" id=\"fnref-foo1-2\" data-footnote-ref>1</a></sup> <a href=\"#fnref-foo2\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"2\" aria-label=\"Back to reference 2\">\u21A9</a></p>\n" +
                    "</li>\n" +
                    "</ol>\n" +
                    "</section>\n"
        )
    }

    @Test
    fun testNestedFootnotesUnreferenced() {
        // This should not result in any footnotes, as baz itself isn't referenced.
        // But GitHub renders bar only, with a broken backref, because bar is referenced from foo.
        assertRendering(
            "[^foo]: foo[^bar]\n" +
                    "[^bar]: bar\n",
            ""
        )

        // And here only 1 is rendered.
        assertRendering(
            "[^1]\n" +
                    "\n" +
                    "[^1]: one\n" +
                    "[^foo]: foo[^bar]\n" +
                    "[^bar]: bar\n",
            "<p><sup class=\"footnote-ref\"><a href=\"#fn-1\" id=\"fnref-1\" data-footnote-ref>1</a></sup></p>\n" +
                    "<section class=\"footnotes\" data-footnotes>\n" +
                    "<ol>\n" +
                    "<li id=\"fn-1\">\n" +
                    "<p>one <a href=\"#fnref-1\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"1\" aria-label=\"Back to reference 1\">\u21A9</a></p>\n" +
                    "</li>\n" +
                    "</ol>\n" +
                    "</section>\n"
        )
    }

    @Test
    fun testInlineFootnotes() {
        assertRenderingInline(
            "Test ^[inline *footnote*]",
            "<p>Test <sup class=\"footnote-ref\"><a href=\"#fn1\" id=\"fnref1\" data-footnote-ref>1</a></sup></p>\n" +
                    "<section class=\"footnotes\" data-footnotes>\n" +
                    "<ol>\n" +
                    "<li id=\"fn1\">\n" +
                    "<p>inline <em>footnote</em> <a href=\"#fnref1\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"1\" aria-label=\"Back to reference 1\">\u21A9</a></p>\n" +
                    "</li>\n" +
                    "</ol>\n" +
                    "</section>\n"
        )
    }

    @Test
    fun testInlineFootnotesNested() {
        assertRenderingInline(
            "Test ^[inline ^[nested]]",
            "<p>Test <sup class=\"footnote-ref\"><a href=\"#fn1\" id=\"fnref1\" data-footnote-ref>1</a></sup></p>\n" +
                    "<section class=\"footnotes\" data-footnotes>\n" +
                    "<ol>\n" +
                    "<li id=\"fn1\">\n" +
                    "<p>inline <sup class=\"footnote-ref\"><a href=\"#fn2\" id=\"fnref2\" data-footnote-ref>2</a></sup> <a href=\"#fnref1\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"1\" aria-label=\"Back to reference 1\">\u21A9</a></p>\n" +
                    "</li>\n" +
                    "<li id=\"fn2\">\n" +
                    "<p>nested <a href=\"#fnref2\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"2\" aria-label=\"Back to reference 2\">\u21A9</a></p>\n" +
                    "</li>\n" +
                    "</ol>\n" +
                    "</section>\n"
        )
    }

    @Test
    fun testInlineFootnoteWithReference() {
        // This is a bit tricky because the IDs need to be unique.
        assertRenderingInline(
            "Test ^[inline [^1]]\n" +
                    "\n" +
                    "[^1]: normal",
            "<p>Test <sup class=\"footnote-ref\"><a href=\"#fn1\" id=\"fnref1\" data-footnote-ref>1</a></sup></p>\n" +
                    "<section class=\"footnotes\" data-footnotes>\n" +
                    "<ol>\n" +
                    "<li id=\"fn1\">\n" +
                    "<p>inline <sup class=\"footnote-ref\"><a href=\"#fn-1\" id=\"fnref-1\" data-footnote-ref>2</a></sup> <a href=\"#fnref1\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"1\" aria-label=\"Back to reference 1\">\u21A9</a></p>\n" +
                    "</li>\n" +
                    "<li id=\"fn-1\">\n" +
                    "<p>normal <a href=\"#fnref-1\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"2\" aria-label=\"Back to reference 2\">\u21A9</a></p>\n" +
                    "</li>\n" +
                    "</ol>\n" +
                    "</section>\n"
        )
    }

    @Test
    fun testInlineFootnoteInsideDefinition() {
        assertRenderingInline(
            "Test [^1]\n" +
                    "\n" +
                    "[^1]: Definition ^[inline]\n",
            "<p>Test <sup class=\"footnote-ref\"><a href=\"#fn-1\" id=\"fnref-1\" data-footnote-ref>1</a></sup></p>\n" +
                    "<section class=\"footnotes\" data-footnotes>\n" +
                    "<ol>\n" +
                    "<li id=\"fn-1\">\n" +
                    "<p>Definition <sup class=\"footnote-ref\"><a href=\"#fn2\" id=\"fnref2\" data-footnote-ref>2</a></sup> <a href=\"#fnref-1\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"1\" aria-label=\"Back to reference 1\">\u21A9</a></p>\n" +
                    "</li>\n" +
                    "<li id=\"fn2\">\n" +
                    "<p>inline <a href=\"#fnref2\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"2\" aria-label=\"Back to reference 2\">\u21A9</a></p>\n" +
                    "</li>\n" +
                    "</ol>\n" +
                    "</section>\n"
        )
    }

    @Test
    fun testInlineFootnoteInsideDefinition2() {
        // Tricky because of the nested inline footnote which we want to visit after foo (breadth-first).
        assertRenderingInline(
            "Test [^1]\n" +
                    "\n" +
                    "[^1]: Definition ^[inline ^[nested]] ^[foo]\n",
            "<p>Test <sup class=\"footnote-ref\"><a href=\"#fn-1\" id=\"fnref-1\" data-footnote-ref>1</a></sup></p>\n" +
                    "<section class=\"footnotes\" data-footnotes>\n" +
                    "<ol>\n" +
                    "<li id=\"fn-1\">\n" +
                    "<p>Definition <sup class=\"footnote-ref\"><a href=\"#fn2\" id=\"fnref2\" data-footnote-ref>2</a></sup> <sup class=\"footnote-ref\"><a href=\"#fn3\" id=\"fnref3\" data-footnote-ref>3</a></sup> <a href=\"#fnref-1\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"1\" aria-label=\"Back to reference 1\">\u21A9</a></p>\n" +
                    "</li>\n" +
                    "<li id=\"fn2\">\n" +
                    "<p>inline <sup class=\"footnote-ref\"><a href=\"#fn4\" id=\"fnref4\" data-footnote-ref>4</a></sup> <a href=\"#fnref2\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"2\" aria-label=\"Back to reference 2\">\u21A9</a></p>\n" +
                    "</li>\n" +
                    "<li id=\"fn3\">\n" +
                    "<p>foo <a href=\"#fnref3\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"3\" aria-label=\"Back to reference 3\">\u21A9</a></p>\n" +
                    "</li>\n" +
                    "<li id=\"fn4\">\n" +
                    "<p>nested <a href=\"#fnref4\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"4\" aria-label=\"Back to reference 4\">\u21A9</a></p>\n" +
                    "</li>\n" +
                    "</ol>\n" +
                    "</section>\n"
        )
    }

    @Test
    fun testRenderNodesDirectly() {
        // Everything should work as expected when rendering from nodes directly (no parsing step).
        val doc = Document()
        val p = Paragraph()
        p.appendChild(Text("Test "))
        p.appendChild(FootnoteReference("foo"))
        val def = FootnoteDefinition("foo")
        val note = Paragraph()
        note.appendChild(Text("note!"))
        def.appendChild(note)
        doc.appendChild(p)
        doc.appendChild(def)

        val expected =
            "<p>Test <sup class=\"footnote-ref\"><a href=\"#fn-foo\" id=\"fnref-foo\" data-footnote-ref>1</a></sup></p>\n" +
                    "<section class=\"footnotes\" data-footnotes>\n" +
                    "<ol>\n" +
                    "<li id=\"fn-foo\">\n" +
                    "<p>note! <a href=\"#fnref-foo\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"1\" aria-label=\"Back to reference 1\">\u21A9</a></p>\n" +
                    "</li>\n" +
                    "</ol>\n" +
                    "</section>\n"
        assertEquals(expected, renderer.render(doc))
    }

    private fun assertRenderingInline(source: String, expected: String) {
        val extension = FootnotesExtension.builder().inlineFootnotes(true).build()
        val inlineParser = Parser.builder().extensions(listOf(extension)).build()
        val inlineRenderer = HtmlRenderer.builder().extensions(listOf(extension)).build()
        assertEquals(expected, inlineRenderer.render(inlineParser.parse(source)))
    }
}
