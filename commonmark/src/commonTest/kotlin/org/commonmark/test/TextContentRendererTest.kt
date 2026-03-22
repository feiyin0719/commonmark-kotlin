package org.commonmark.test

import org.commonmark.node.Link
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.NodeRenderer
import org.commonmark.renderer.text.LineBreakRendering
import org.commonmark.renderer.text.TextContentNodeRendererContext
import org.commonmark.renderer.text.TextContentNodeRendererFactory
import org.commonmark.renderer.text.TextContentRenderer
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals

class TextContentRendererTest {
    @Test
    fun textContentText() {
        var s: String

        s = "foo bar"
        assertCompact(s, "foo bar")
        assertStripped(s, "foo bar")

        s = "foo foo\n\nbar\nbar"
        assertCompact(s, "foo foo\nbar\nbar")
        assertSeparate(s, "foo foo\n\nbar\nbar")
        assertStripped(s, "foo foo bar bar")
    }

    @Test
    fun textContentHeading() {
        assertCompact("# Heading\n\nFoo", "Heading\nFoo")
        assertSeparate("# Heading\n\nFoo", "Heading\n\nFoo")
        assertStripped("# Heading\n\nFoo", "Heading: Foo")
    }

    @Test
    fun textContentEmphasis() {
        var s: String

        s = "***foo***"
        assertCompact(s, "foo")
        assertStripped(s, "foo")

        s = "foo ***foo*** bar ***bar***"
        assertCompact(s, "foo foo bar bar")
        assertStripped(s, "foo foo bar bar")

        s = "foo\n***foo***\nbar\n\n***bar***"
        assertCompact(s, "foo\nfoo\nbar\nbar")
        assertSeparate(s, "foo\nfoo\nbar\n\nbar")
        assertStripped(s, "foo foo bar bar")
    }

    @Test
    fun textContentQuotes() {
        val s = "foo\n>foo\nbar\n\nbar"
        assertCompact(s, "foo\n\u00ABfoo\nbar\u00BB\nbar")
        assertSeparate(s, "foo\n\n\u00ABfoo\nbar\u00BB\n\nbar")
        assertStripped(s, "foo \u00ABfoo bar\u00BB bar")
    }

    @Test
    fun textContentLinks() {
        assertAll("foo [text](http://link \"title\") bar", "foo \"text\" (title: http://link) bar")
        assertAll("foo [text](http://link \"http://link\") bar", "foo \"text\" (http://link) bar")
        assertAll("foo [text](http://link) bar", "foo \"text\" (http://link) bar")
        assertAll("foo [text]() bar", "foo \"text\" bar")
        assertAll("foo http://link bar", "foo http://link bar")
    }

    @Test
    fun textContentImages() {
        assertAll("foo ![text](http://link \"title\") bar", "foo \"text\" (title: http://link) bar")
        assertAll("foo ![text](http://link) bar", "foo \"text\" (http://link) bar")
        assertAll("foo ![text]() bar", "foo \"text\" bar")
    }

    @Test
    fun textContentLists() {
        var s: String

        s = "foo\n* foo\n* bar\n\nbar"
        assertCompact(s, "foo\n* foo\n* bar\nbar")
        assertSeparate(s, "foo\n\n* foo\n* bar\n\nbar")
        assertStripped(s, "foo foo bar bar")

        s = "foo\n- foo\n- bar\n\nbar"
        assertCompact(s, "foo\n- foo\n- bar\nbar")
        assertSeparate(s, "foo\n\n- foo\n- bar\n\nbar")
        assertStripped(s, "foo foo bar bar")

        s = "foo\n1. foo\n2. bar\n\nbar"
        assertCompact(s, "foo\n1. foo\n2. bar\nbar")
        assertSeparate(s, "foo\n\n1. foo\n2. bar\n\nbar")
        assertStripped(s, "foo 1. foo 2. bar bar")

        s = "foo\n0) foo\n1) bar\n\nbar"
        assertCompact(s, "foo\n0) foo\n1) bar\nbar")
        assertSeparate(s, "foo\n0) foo\n\n1) bar\n\nbar")
        assertStripped(s, "foo 0) foo 1) bar bar")

        s = "bar\n1. foo\n   1. bar\n2. foo"
        assertCompact(s, "bar\n1. foo\n   1. bar\n2. foo")
        assertSeparate(s, "bar\n\n1. foo\n   1. bar\n2. foo")
        assertStripped(s, "bar 1. foo 1. bar 2. foo")

        s = "bar\n* foo\n  - bar\n* foo"
        assertCompact(s, "bar\n* foo\n  - bar\n* foo")
        assertSeparate(s, "bar\n\n* foo\n  - bar\n* foo")
        assertStripped(s, "bar foo bar foo")

        s = "bar\n* foo\n  1. bar\n  2. bar\n* foo"
        assertCompact(s, "bar\n* foo\n  1. bar\n  2. bar\n* foo")
        assertSeparate(s, "bar\n\n* foo\n  1. bar\n  2. bar\n* foo")
        assertStripped(s, "bar foo 1. bar 2. bar foo")

        s = "bar\n1. foo\n   * bar\n   * bar\n2. foo"
        assertCompact(s, "bar\n1. foo\n   * bar\n   * bar\n2. foo")
        assertSeparate(s, "bar\n\n1. foo\n   * bar\n   * bar\n2. foo")
        assertStripped(s, "bar 1. foo bar bar 2. foo")

        // For a loose list (not tight)
        s = "foo\n\n* bar\n\n* baz"
        // Compact ignores loose
        assertCompact(s, "foo\n* bar\n* baz")
        // Separate preserves it
        assertSeparate(s, "foo\n\n* bar\n\n* baz")
        assertStripped(s, "foo bar baz")
    }

    @Test
    fun textContentCode() {
        assertAll("foo `code` bar", "foo \"code\" bar")
    }

    @Test
    fun textContentCodeBlock() {
        var s: String
        s = "foo\n```\nfoo\nbar\n```\nbar"
        assertCompact(s, "foo\nfoo\nbar\nbar")
        assertSeparate(s, "foo\n\nfoo\nbar\n\nbar")
        assertStripped(s, "foo foo bar bar")

        s = "foo\n\n    foo\n     bar\nbar"
        assertCompact(s, "foo\nfoo\n bar\nbar")
        assertSeparate(s, "foo\n\nfoo\n bar\n\nbar")
        assertStripped(s, "foo foo bar bar")
    }

    @Test
    fun textContentBreaks() {
        var s: String

        s = "foo\nbar"
        assertCompact(s, "foo\nbar")
        assertSeparate(s, "foo\nbar")
        assertStripped(s, "foo bar")

        s = "foo  \nbar"
        assertCompact(s, "foo\nbar")
        assertSeparate(s, "foo\nbar")
        assertStripped(s, "foo bar")

        s = "foo\n___\nbar"
        assertCompact(s, "foo\n***\nbar")
        assertSeparate(s, "foo\n\n***\n\nbar")
        assertStripped(s, "foo bar")
    }

    @Test
    fun textContentHtml() {
        var html =
            "<table>\n" +
                "  <tr>\n" +
                "    <td>\n" +
                "           foobar\n" +
                "    </td>\n" +
                "  </tr>\n" +
                "</table>"
        assertCompact(html, html)
        assertSeparate(html, html)

        html = "foo <foo>foobar</foo> bar"
        assertAll(html, html)
    }

    @Test
    fun testContentNestedLists() {
        val s =
            "List:\n" +
                "1. 2) 3. \n" +
                "end"
        assertCompact(s, s)

        val s2 = "1. A\n   1) B\n      1. Test"
        assertCompact(s2, s2)
    }

    @Test
    fun testOverrideNodeRendering() {
        val nodeRendererFactory =
            TextContentNodeRendererFactory { context ->
                object : NodeRenderer {
                    override fun getNodeTypes(): Set<KClass<out Node>> = setOf(Link::class)

                    override fun render(node: Node) {
                        context.getWriter().write('"')
                        renderChildren(node)
                        context.getWriter().write('"')
                    }

                    private fun renderChildren(parent: Node) {
                        var node = parent.firstChild
                        while (node != null) {
                            val next = node.next
                            context.render(node)
                            node = next
                        }
                    }
                }
            }
        val renderer = TextContentRenderer.builder().nodeRendererFactory(nodeRendererFactory).build()
        val source = "Hi [Example](https://example.com)"
        assertRendering(source, "Hi \"Example\"", renderer.render(PARSER.parse(source)))
    }

    private fun assertCompact(
        source: String,
        expected: String,
    ) {
        val doc = PARSER.parse(source)
        val actualRendering = COMPACT_RENDERER.render(doc)
        assertRendering(source, expected, actualRendering)
    }

    private fun assertSeparate(
        source: String,
        expected: String,
    ) {
        val doc = PARSER.parse(source)
        val actualRendering = SEPARATE_RENDERER.render(doc)
        assertRendering(source, expected, actualRendering)
    }

    private fun assertStripped(
        source: String,
        expected: String,
    ) {
        val doc = PARSER.parse(source)
        val actualRendering = STRIPPED_RENDERER.render(doc)
        assertRendering(source, expected, actualRendering)
    }

    private fun assertAll(
        source: String,
        expected: String,
    ) {
        assertCompact(source, expected)
        assertSeparate(source, expected)
        assertStripped(source, expected)
    }

    companion object {
        private val PARSER = Parser.builder().build()
        private val COMPACT_RENDERER = TextContentRenderer.builder().build()
        private val SEPARATE_RENDERER =
            TextContentRenderer
                .builder()
                .lineBreakRendering(LineBreakRendering.SEPARATE_BLOCKS)
                .build()
        private val STRIPPED_RENDERER =
            TextContentRenderer
                .builder()
                .lineBreakRendering(LineBreakRendering.STRIP)
                .build()

        private fun showTabs(s: String): String = s.replace("\t", "\u2192")

        private fun assertRendering(
            source: String,
            expectedRendering: String,
            actualRendering: String,
        ) {
            // include source for better assertion errors
            val expected = showTabs("$expectedRendering\n\n$source")
            val actual = showTabs("$actualRendering\n\n$source")
            assertEquals(expected, actual)
        }
    }
}
