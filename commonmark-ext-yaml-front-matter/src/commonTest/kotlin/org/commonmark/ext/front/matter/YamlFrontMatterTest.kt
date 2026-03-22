package org.commonmark.ext.front.matter

import org.commonmark.Extension
import org.commonmark.node.CustomNode
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class YamlFrontMatterTest {
    private val extensions: Set<Extension> = setOf(YamlFrontMatterExtension.create())
    private val parser: Parser = Parser.builder().extensions(extensions).build()
    private val renderer: HtmlRenderer = HtmlRenderer.builder().extensions(extensions).build()

    private fun render(source: String): String = renderer.render(parser.parse(source))

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
    fun simpleValue() {
        val input =
            "---" +
                "\nhello: world" +
                "\n..." +
                "\n" +
                "\ngreat"
        val rendered = "<p>great</p>\n"

        val data = getFrontMatter(input)

        assertEquals(1, data.size)
        assertEquals("hello", data.keys.iterator().next())
        assertEquals(1, data["hello"]!!.size)
        assertEquals("world", data["hello"]!![0])

        assertRendering(input, rendered)
    }

    @Test
    fun emptyValue() {
        val input =
            "---" +
                "\nkey:" +
                "\n---" +
                "\n" +
                "\ngreat"
        val rendered = "<p>great</p>\n"

        val data = getFrontMatter(input)

        assertEquals(1, data.size)
        assertEquals("key", data.keys.iterator().next())
        assertEquals(0, data["key"]!!.size)

        assertRendering(input, rendered)
    }

    @Test
    fun listValues() {
        val input =
            "---" +
                "\nlist:" +
                "\n  - value1" +
                "\n  - value2" +
                "\n..." +
                "\n" +
                "\ngreat"
        val rendered = "<p>great</p>\n"

        val data = getFrontMatter(input)

        assertEquals(1, data.size)
        assertTrue(data.containsKey("list"))
        assertEquals(2, data["list"]!!.size)
        assertEquals("value1", data["list"]!![0])
        assertEquals("value2", data["list"]!![1])

        assertRendering(input, rendered)
    }

    @Test
    fun literalValue1() {
        val input =
            "---" +
                "\nliteral: |" +
                "\n  hello markdown!" +
                "\n  literal thing..." +
                "\n---" +
                "\n" +
                "\ngreat"
        val rendered = "<p>great</p>\n"

        val data = getFrontMatter(input)

        assertEquals(1, data.size)
        assertTrue(data.containsKey("literal"))
        assertEquals(1, data["literal"]!!.size)
        assertEquals("hello markdown!\nliteral thing...", data["literal"]!![0])

        assertRendering(input, rendered)
    }

    @Test
    fun literalValue2() {
        val input =
            "---" +
                "\nliteral: |" +
                "\n  - hello markdown!" +
                "\n---" +
                "\n" +
                "\ngreat"
        val rendered = "<p>great</p>\n"

        val data = getFrontMatter(input)

        assertEquals(1, data.size)
        assertTrue(data.containsKey("literal"))
        assertEquals(1, data["literal"]!!.size)
        assertEquals("- hello markdown!", data["literal"]!![0])

        assertRendering(input, rendered)
    }

    @Test
    fun complexValues() {
        val input =
            "---" +
                "\nsimple: value" +
                "\nliteral: |" +
                "\n  hello markdown!" +
                "\n" +
                "\n  literal literal" +
                "\nlist:" +
                "\n    - value1" +
                "\n    - value2" +
                "\n---" +
                "\ngreat"
        val rendered = "<p>great</p>\n"

        val data = getFrontMatter(input)

        assertEquals(3, data.size)

        assertTrue(data.containsKey("simple"))
        assertEquals(1, data["simple"]!!.size)
        assertEquals("value", data["simple"]!![0])

        assertTrue(data.containsKey("literal"))
        assertEquals(1, data["literal"]!!.size)
        assertEquals("hello markdown!\n\nliteral literal", data["literal"]!![0])

        assertTrue(data.containsKey("list"))
        assertEquals(2, data["list"]!!.size)
        assertEquals("value1", data["list"]!![0])
        assertEquals("value2", data["list"]!![1])

        assertRendering(input, rendered)
    }

    @Test
    fun empty() {
        val input =
            "---\n" +
                "---\n" +
                "test"
        val rendered = "<p>test</p>\n"

        val data = getFrontMatter(input)

        assertTrue(data.isEmpty())

        assertRendering(input, rendered)
    }

    @Test
    fun yamlInParagraph() {
        val input =
            "# hello\n" +
                "\nhello markdown world!" +
                "\n---" +
                "\nhello: world" +
                "\n---"
        val rendered = "<h1>hello</h1>\n<h2>hello markdown world!</h2>\n<h2>hello: world</h2>\n"

        val data = getFrontMatter(input)

        assertTrue(data.isEmpty())

        assertRendering(input, rendered)
    }

    @Test
    fun yamlOnSecondLine() {
        val input =
            "hello\n" +
                "\n---" +
                "\nhello: world" +
                "\n---"
        val rendered = "<p>hello</p>\n<hr />\n<h2>hello: world</h2>\n"

        val data = getFrontMatter(input)

        assertTrue(data.isEmpty())

        assertRendering(input, rendered)
    }

    @Test
    fun nonMatchedStartTag() {
        val input =
            "----\n" +
                "test"
        val rendered = "<hr />\n<p>test</p>\n"

        val data = getFrontMatter(input)

        assertTrue(data.isEmpty())

        assertRendering(input, rendered)
    }

    @Test
    fun inList() {
        val input =
            "* ---\n" +
                "  ---\n" +
                "test"
        val rendered = "<ul>\n<li>\n<hr />\n<hr />\n</li>\n</ul>\n<p>test</p>\n"

        val data = getFrontMatter(input)

        assertTrue(data.isEmpty())

        assertRendering(input, rendered)
    }

    @Test
    fun visitorIgnoresOtherCustomNodes() {
        val input =
            "---" +
                "\nhello: world" +
                "\n---" +
                "\n"

        val visitor = YamlFrontMatterVisitor()
        val document = parser.parse(input)
        document.appendChild(TestNode())
        document.accept(visitor)

        val data = visitor.data
        assertEquals(1, data.size)
        assertTrue(data.containsKey("hello"))
        assertEquals(listOf("world"), data["hello"])
    }

    @Test
    fun nodesCanBeModified() {
        val input =
            "---" +
                "\nhello: world" +
                "\n---" +
                "\n"

        val document = parser.parse(input)
        val node = document.firstChild!!.firstChild!! as YamlFrontMatterNode
        node.key = "see"
        node.values = listOf("you")

        val visitor = YamlFrontMatterVisitor()
        document.accept(visitor)

        val data = visitor.data
        assertEquals(1, data.size)
        assertTrue(data.containsKey("see"))
        assertEquals(listOf("you"), data["see"])
    }

    @Test
    fun dotInKeys() {
        val input =
            "---" +
                "\nms.author: author" +
                "\n---" +
                "\n"

        val data = getFrontMatter(input)

        assertEquals(1, data.size)
        assertEquals("ms.author", data.keys.iterator().next())
        assertEquals(1, data["ms.author"]!!.size)
        assertEquals("author", data["ms.author"]!![0])
    }

    @Test
    fun singleQuotedLiterals() {
        val input =
            "---" +
                "\nstring: 'It''s me'" +
                "\nlist:" +
                "\n  - 'I''m here'" +
                "\n---" +
                "\n"

        val data = getFrontMatter(input)

        assertEquals(2, data.size)
        assertEquals("It's me", data["string"]!![0])
        assertEquals("I'm here", data["list"]!![0])
    }

    @Test
    fun doubleQuotedLiteral() {
        val input =
            "---" +
                "\nstring: \"backslash: \\\\ quote: \\\"\"" +
                "\nlist:" +
                "\n  - \"hey\"" +
                "\n---" +
                "\n"

        val data = getFrontMatter(input)

        assertEquals(2, data.size)
        assertEquals("backslash: \\ quote: \"", data["string"]!![0])
        assertEquals("hey", data["list"]!![0])
    }

    private fun getFrontMatter(input: String): Map<String, List<String>> {
        val visitor = YamlFrontMatterVisitor()
        val document = parser.parse(input)
        document.accept(visitor)
        return visitor.data
    }

    // Custom node for tests
    private class TestNode : CustomNode()

    companion object {
        private fun showTabs(s: String): String = s.replace("\t", "\u2192")
    }
}
