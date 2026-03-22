package org.commonmark.test

import org.commonmark.node.AbstractVisitor
import org.commonmark.node.Delimited
import org.commonmark.node.Emphasis
import org.commonmark.node.StrongEmphasis
import org.commonmark.parser.Parser
import kotlin.test.Test
import kotlin.test.assertEquals

class DelimitedTest {

    @Test
    fun emphasisDelimiters() {
        val input = "* *emphasis* \n" +
                "* **strong** \n" +
                "* _important_ \n" +
                "* __CRITICAL__ \n"

        val parser = Parser.builder().build()
        val document = parser.parse(input)

        val list = mutableListOf<Delimited>()
        val visitor = object : AbstractVisitor() {
            override fun visit(node: Emphasis) {
                list.add(node)
            }

            override fun visit(node: StrongEmphasis) {
                list.add(node)
            }
        }
        document.accept(visitor)

        assertEquals(4, list.size)

        val emphasis = list[0]
        val strong = list[1]
        val important = list[2]
        val critical = list[3]

        assertEquals("*", emphasis.openingDelimiter)
        assertEquals("*", emphasis.closingDelimiter)
        assertEquals("**", strong.openingDelimiter)
        assertEquals("**", strong.closingDelimiter)
        assertEquals("_", important.openingDelimiter)
        assertEquals("_", important.closingDelimiter)
        assertEquals("__", critical.openingDelimiter)
        assertEquals("__", critical.closingDelimiter)
    }
}
