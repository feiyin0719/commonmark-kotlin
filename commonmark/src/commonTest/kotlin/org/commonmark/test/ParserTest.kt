package org.commonmark.test

import org.commonmark.node.Block
import org.commonmark.node.BulletList
import org.commonmark.node.Heading
import org.commonmark.node.Text
import org.commonmark.node.ThematicBreak
import org.commonmark.parser.InlineParser
import org.commonmark.parser.InlineParserFactory
import org.commonmark.parser.Parser
import org.commonmark.parser.SourceLines
import org.commonmark.node.Node
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ParserTest {

    @Test
    fun enabledBlockTypes() {
        val given = "# heading 1\n\nnot a heading"

        var parser = Parser.builder().build() // all core parsers by default
        var document = parser.parse(given)
        assertTrue(document.firstChild is Heading)

        val headersOnly = setOf<KClass<out Block>>(Heading::class)
        parser = Parser.builder().enabledBlockTypes(headersOnly).build()
        document = parser.parse(given)
        assertTrue(document.firstChild is Heading)

        val noCoreTypes = emptySet<KClass<out Block>>()
        parser = Parser.builder().enabledBlockTypes(noCoreTypes).build()
        document = parser.parse(given)
        assertFalse(document.firstChild is Heading)
    }

    @Test
    fun enabledBlockTypesThrowsWhenGivenUnknownClass() {
        // BulletList can't be enabled separately at the moment, only all ListBlock types
        assertFailsWith<IllegalArgumentException> {
            Parser.builder().enabledBlockTypes(setOf(Heading::class, BulletList::class)).build()
        }
    }

    @Test
    fun indentation() {
        val given = " - 1 space\n   - 3 spaces\n     - 5 spaces\n\t - tab + space"
        val parser = Parser.builder().build()
        val document = parser.parse(given)

        assertTrue(document.firstChild is BulletList)

        var list = document.firstChild!! // first level list
        assertEquals(list.firstChild, list.lastChild, "expect one child")
        assertEquals("1 space", firstText(list.firstChild))

        list = list.firstChild!!.lastChild!! // second level list
        assertEquals(list.firstChild, list.lastChild, "expect one child")
        assertEquals("3 spaces", firstText(list.firstChild))

        list = list.firstChild!!.lastChild!! // third level list
        assertEquals("5 spaces", firstText(list.firstChild))
        assertEquals("tab + space", firstText(list.firstChild!!.next))
    }

    @Test
    fun inlineParser() {
        val fakeInlineParser = object : InlineParser {
            override fun parse(lines: SourceLines, node: Node) {
                node.appendChild(ThematicBreak())
            }
        }

        val fakeInlineParserFactory = InlineParserFactory { fakeInlineParser }

        val parser = Parser.builder().inlineParserFactory(fakeInlineParserFactory).build()
        val input = "**bold** **bold** ~~strikethrough~~"

        assertTrue(parser.parse(input).firstChild!!.firstChild is ThematicBreak)
    }

    private fun firstText(n: Node?): String {
        var node = n
        while (node !is Text) {
            assertNotNull(node)
            node = node.firstChild
        }
        return node.literal
    }
}
