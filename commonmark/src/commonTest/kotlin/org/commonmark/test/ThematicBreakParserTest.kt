package org.commonmark.test

import org.commonmark.node.ThematicBreak
import org.commonmark.parser.Parser
import kotlin.test.Test
import kotlin.test.assertEquals

class ThematicBreakParserTest {

    private val parser = Parser.builder().build()

    @Test
    fun testLiteral() {
        assertLiteral("***", "***")
        assertLiteral("-- -", "-- -")
        assertLiteral("  __  __  __  ", "  __  __  __  ")
        assertLiteral("***", "> ***")
    }

    private fun assertLiteral(expected: String, input: String) {
        val tb = parser.parse(input).find<ThematicBreak>()
        assertEquals(expected, tb.literal)
    }
}
