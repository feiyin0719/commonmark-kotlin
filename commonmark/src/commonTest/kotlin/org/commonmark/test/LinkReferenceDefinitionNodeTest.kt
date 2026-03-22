package org.commonmark.test

import org.commonmark.node.BulletList
import org.commonmark.node.Heading
import org.commonmark.node.LinkReferenceDefinition
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.Paragraph
import org.commonmark.parser.Parser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LinkReferenceDefinitionNodeTest {
    @Test
    fun testDefinitionWithoutParagraph() {
        val document = parse("This is a paragraph with a [foo] link.\n\n[foo]: /url 'title'")
        val nodes = TestNodes.getChildren(document)

        assertEquals(2, nodes.size)
        assertTrue(nodes[0] is Paragraph)
        val definition = assertDef(nodes[1], "foo")

        assertEquals("/url", definition.destination)
        assertEquals("title", definition.title)
    }

    @Test
    fun testDefinitionWithParagraph() {
        val document = parse("[foo]: /url\nThis is a paragraph with a [foo] link.")
        val nodes = TestNodes.getChildren(document)

        assertEquals(2, nodes.size)
        // Note that definition is not part of the paragraph, it's a sibling
        assertTrue(nodes[0] is LinkReferenceDefinition)
        assertTrue(nodes[1] is Paragraph)
    }

    @Test
    fun testMultipleDefinitions() {
        val document = parse("This is a paragraph with a [foo] link.\n\n[foo]: /url\n[bar]: /url")
        val nodes = TestNodes.getChildren(document)

        assertEquals(3, nodes.size)
        assertTrue(nodes[0] is Paragraph)
        assertDef(nodes[1], "foo")
        assertDef(nodes[2], "bar")
    }

    @Test
    fun testMultipleDefinitionsWithSameLabel() {
        val document = parse("This is a paragraph with a [foo] link.\n\n[foo]: /url1\n[foo]: /url2")
        val nodes = TestNodes.getChildren(document)

        assertEquals(3, nodes.size)
        assertTrue(nodes[0] is Paragraph)
        val def1 = assertDef(nodes[1], "foo")
        assertEquals("/url1", def1.destination)
        // When there's multiple definitions with the same label, the first one "wins", as in reference links will use
        // that. But we still want to preserve the original definitions in the document.
        val def2 = assertDef(nodes[2], "foo")
        assertEquals("/url2", def2.destination)
    }

    @Test
    fun testDefinitionOfReplacedBlock() {
        val document = parse("[foo]: /url\nHeading\n=======")
        val nodes = TestNodes.getChildren(document)

        assertEquals(2, nodes.size)
        assertDef(nodes[0], "foo")
        assertTrue(nodes[1] is Heading)
    }

    @Test
    fun testDefinitionInListItem() {
        val document = parse("* [foo]: /url\n  [foo]\n")
        assertTrue(document.firstChild is BulletList)
        val item = document.firstChild!!.firstChild
        assertTrue(item is ListItem)

        val nodes = TestNodes.getChildren(item!!)
        assertEquals(2, nodes.size)
        assertDef(nodes[0], "foo")
        assertTrue(nodes[1] is Paragraph)
    }

    @Test
    fun testDefinitionInListItem2() {
        val document = parse("* [foo]: /url\n* [foo]\n")
        assertTrue(document.firstChild is BulletList)

        val items = TestNodes.getChildren(document.firstChild!!)
        assertEquals(2, items.size)
        val item1 = items[0]
        val item2 = items[1]

        assertTrue(item1 is ListItem)
        assertTrue(item2 is ListItem)

        assertEquals(1, TestNodes.getChildren(item1).size)
        assertDef(item1.firstChild!!, "foo")

        assertEquals(1, TestNodes.getChildren(item2).size)
        assertTrue(item2.firstChild is Paragraph)
    }

    @Test
    fun testDefinitionLabelCaseIsPreserved() {
        val document = parse("This is a paragraph with a [foo] link.\n\n[fOo]: /url 'title'")
        val nodes = TestNodes.getChildren(document)

        assertEquals(2, nodes.size)
        assertTrue(nodes[0] is Paragraph)
        assertDef(nodes[1], "fOo")
    }

    companion object {
        private fun parse(input: String): Node {
            val parser = Parser.builder().build()
            return parser.parse(input)
        }

        private fun assertDef(
            node: Node,
            label: String,
        ): LinkReferenceDefinition {
            assertTrue(node is LinkReferenceDefinition)
            assertEquals(label, node.label)
            return node
        }
    }
}
