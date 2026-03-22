package org.commonmark.test

import org.commonmark.node.SourceSpan
import org.commonmark.parser.SourceLine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SourceLineTest {

    @Test
    fun testSubstring() {
        val line = SourceLine.of("abcd", SourceSpan.of(3, 10, 13, 4))

        assertSourceLine(line.substring(0, 4), "abcd", SourceSpan.of(3, 10, 13, 4))
        assertSourceLine(line.substring(0, 3), "abc", SourceSpan.of(3, 10, 13, 3))
        assertSourceLine(line.substring(0, 2), "ab", SourceSpan.of(3, 10, 13, 2))
        assertSourceLine(line.substring(0, 1), "a", SourceSpan.of(3, 10, 13, 1))
        assertSourceLine(line.substring(0, 0), "", null)

        assertSourceLine(line.substring(1, 4), "bcd", SourceSpan.of(3, 11, 14, 3))
        assertSourceLine(line.substring(1, 3), "bc", SourceSpan.of(3, 11, 14, 2))

        assertSourceLine(line.substring(3, 4), "d", SourceSpan.of(3, 13, 16, 1))
        assertSourceLine(line.substring(4, 4), "", null)
    }

    @Test
    fun testSubstringBeginOutOfBounds() {
        val sourceLine = SourceLine.of("abcd", SourceSpan.of(3, 10, 13, 4))
        assertFailsWith<StringIndexOutOfBoundsException> { sourceLine.substring(3, 2) }
    }

    @Test
    fun testSubstringEndOutOfBounds() {
        val sourceLine = SourceLine.of("abcd", SourceSpan.of(3, 10, 13, 4))
        assertFailsWith<StringIndexOutOfBoundsException> { sourceLine.substring(0, 5) }
    }

    companion object {
        private fun assertSourceLine(sourceLine: SourceLine, expectedContent: String, expectedSourceSpan: SourceSpan?) {
            assertEquals(expectedContent, sourceLine.content.toString())
            assertEquals(expectedSourceSpan, sourceLine.sourceSpan)
        }
    }
}
