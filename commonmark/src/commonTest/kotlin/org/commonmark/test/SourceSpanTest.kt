package org.commonmark.test

import org.commonmark.node.SourceSpan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class SourceSpanTest {

    @Test
    fun testSubSpan() {
        val span = SourceSpan.of(1, 2, 3, 5)

        assertSame(span, span.subSpan(0))
        assertSame(span, span.subSpan(0, 5))

        assertEquals(SourceSpan.of(1, 3, 4, 4), span.subSpan(1))
        assertEquals(SourceSpan.of(1, 4, 5, 3), span.subSpan(2))
        assertEquals(SourceSpan.of(1, 5, 6, 2), span.subSpan(3))
        assertEquals(SourceSpan.of(1, 6, 7, 1), span.subSpan(4))
        // Not sure if empty spans are useful, but it probably makes sense to mirror how substrings work
        assertEquals(SourceSpan.of(1, 7, 8, 0), span.subSpan(5))
        assertEquals("", "abcde".substring(5))

        assertEquals(SourceSpan.of(1, 2, 3, 5), span.subSpan(0, 5))
        assertEquals(SourceSpan.of(1, 2, 3, 4), span.subSpan(0, 4))
        assertEquals(SourceSpan.of(1, 2, 3, 3), span.subSpan(0, 3))
        assertEquals(SourceSpan.of(1, 2, 3, 2), span.subSpan(0, 2))
        assertEquals(SourceSpan.of(1, 2, 3, 1), span.subSpan(0, 1))
        assertEquals(SourceSpan.of(1, 2, 3, 0), span.subSpan(0, 0))
        assertEquals("a", "abcde".substring(0, 1))
        assertEquals("", "abcde".substring(0, 0))

        assertEquals(SourceSpan.of(1, 3, 4, 3), span.subSpan(1, 4))
        assertEquals(SourceSpan.of(1, 4, 5, 1), span.subSpan(2, 3))
    }

    @Test
    fun testSubSpanBeginIndexNegative() {
        val sourceSpan = SourceSpan.of(1, 2, 3, 5)
        assertFailsWith<IllegalArgumentException> { sourceSpan.subSpan(-1) }
    }

    @Test
    fun testSubSpanBeginIndexOutOfBounds() {
        val sourceSpan = SourceSpan.of(1, 2, 3, 5)
        assertFailsWith<IllegalArgumentException> { sourceSpan.subSpan(6) }
    }

    @Test
    fun testSubSpanEndIndexNegative() {
        val sourceSpan = SourceSpan.of(1, 2, 3, 5)
        assertFailsWith<IllegalArgumentException> { sourceSpan.subSpan(0, -1) }
    }

    @Test
    fun testSubSpanEndIndexOutOfBounds() {
        val sourceSpan = SourceSpan.of(1, 2, 3, 5)
        assertFailsWith<IllegalArgumentException> { sourceSpan.subSpan(0, 6) }
    }

    @Test
    fun testSubSpanBeginIndexGreaterThanEndIndex() {
        val sourceSpan = SourceSpan.of(1, 2, 3, 5)
        assertFailsWith<IllegalArgumentException> { sourceSpan.subSpan(2, 1) }
    }
}
