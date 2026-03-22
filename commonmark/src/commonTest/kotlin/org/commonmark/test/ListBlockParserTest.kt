package org.commonmark.test

import org.commonmark.node.ListItem
import org.commonmark.parser.Parser
import kotlin.test.Test
import kotlin.test.assertEquals

class ListBlockParserTest {
    private val parser = Parser.builder().build()

    @Test
    fun testBulletListIndents() {
        assertListItemIndents("* foo", 0, 2)
        assertListItemIndents(" * foo", 1, 3)
        assertListItemIndents("  * foo", 2, 4)
        assertListItemIndents("   * foo", 3, 5)

        assertListItemIndents("*  foo", 0, 3)
        assertListItemIndents("*   foo", 0, 4)
        assertListItemIndents("*    foo", 0, 5)
        assertListItemIndents(" *  foo", 1, 4)
        assertListItemIndents("   *    foo", 3, 8)

        // The indent is relative to any containing blocks
        assertListItemIndents("> * foo", 0, 2)
        assertListItemIndents(">  * foo", 1, 3)
        assertListItemIndents(">  *  foo", 1, 4)

        // Tab counts as 3 spaces here (to the next tab stop column of 4) -> content indent is 1+3
        assertListItemIndents("*\tfoo", 0, 4)

        // Empty list, content indent is expected to be 2
        assertListItemIndents("-\n", 0, 2)
    }

    @Test
    fun testOrderedListIndents() {
        assertListItemIndents("1. foo", 0, 3)
        assertListItemIndents(" 1. foo", 1, 4)
        assertListItemIndents("  1. foo", 2, 5)
        assertListItemIndents("   1. foo", 3, 6)

        assertListItemIndents("1.  foo", 0, 4)
        assertListItemIndents("1.   foo", 0, 5)
        assertListItemIndents("1.    foo", 0, 6)
        assertListItemIndents(" 1.  foo", 1, 5)
        assertListItemIndents("  1.    foo", 2, 8)

        assertListItemIndents("> 1. foo", 0, 3)
        assertListItemIndents(">  1. foo", 1, 4)
        assertListItemIndents(">  1.  foo", 1, 5)

        assertListItemIndents("1.\tfoo", 0, 4)
    }

    private fun assertListItemIndents(
        input: String,
        expectedMarkerIndent: Int,
        expectedContentIndent: Int,
    ) {
        val doc = parser.parse(input)
        val listItem = doc.find<ListItem>()
        assertEquals(expectedMarkerIndent, listItem.markerIndent, "markerIndent for input: $input")
        assertEquals(expectedContentIndent, listItem.contentIndent, "contentIndent for input: $input")
    }
}
