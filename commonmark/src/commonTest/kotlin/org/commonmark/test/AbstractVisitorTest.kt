package org.commonmark.test

import org.commonmark.node.AbstractVisitor
import org.commonmark.node.Code
import org.commonmark.node.Node
import org.commonmark.node.Paragraph
import org.commonmark.node.Text
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AbstractVisitorTest {

    @Test
    fun replacingNodeInVisitorShouldNotDestroyVisitOrder() {
        val visitor = object : AbstractVisitor() {
            override fun visit(text: Text) {
                text.insertAfter(Code(text.literal))
                text.unlink()
            }
        }

        val paragraph = Paragraph()
        paragraph.appendChild(Text("foo"))
        paragraph.appendChild(Text("bar"))

        paragraph.accept(visitor)

        assertCode("foo", paragraph.firstChild)
        assertCode("bar", paragraph.firstChild?.next)
        assertNull(paragraph.firstChild?.next?.next)
        assertCode("bar", paragraph.lastChild)
    }

    companion object {
        private fun assertCode(expectedLiteral: String, node: Node?) {
            assertTrue(node is Code)
            assertEquals(expectedLiteral, node.literal)
        }
    }
}
