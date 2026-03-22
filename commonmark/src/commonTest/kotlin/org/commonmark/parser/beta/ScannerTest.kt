package org.commonmark.parser.beta

import org.commonmark.node.SourceSpan
import org.commonmark.parser.SourceLine
import org.commonmark.parser.SourceLines
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScannerTest {

    @Test
    fun testNext() {
        val scanner = Scanner(
            listOf(SourceLine.of("foo bar", null)),
            0, 4
        )
        assertEquals('b', scanner.peek())
        scanner.next()
        assertEquals('a', scanner.peek())
        scanner.next()
        assertEquals('r', scanner.peek())
        scanner.next()
        assertEquals('\u0000', scanner.peek())
    }

    @Test
    fun testMultipleLines() {
        val scanner = Scanner(
            listOf(
                SourceLine.of("ab", null),
                SourceLine.of("cde", null)
            ),
            0, 0
        )
        assertTrue(scanner.hasNext())
        assertEquals('\u0000'.code, scanner.peekPreviousCodePoint())
        assertEquals('a', scanner.peek())
        scanner.next()

        assertTrue(scanner.hasNext())
        assertEquals('a'.code, scanner.peekPreviousCodePoint())
        assertEquals('b', scanner.peek())
        scanner.next()

        assertTrue(scanner.hasNext())
        assertEquals('b'.code, scanner.peekPreviousCodePoint())
        assertEquals('\n', scanner.peek())
        scanner.next()

        assertTrue(scanner.hasNext())
        assertEquals('\n'.code, scanner.peekPreviousCodePoint())
        assertEquals('c', scanner.peek())
        scanner.next()

        assertTrue(scanner.hasNext())
        assertEquals('c'.code, scanner.peekPreviousCodePoint())
        assertEquals('d', scanner.peek())
        scanner.next()

        assertTrue(scanner.hasNext())
        assertEquals('d'.code, scanner.peekPreviousCodePoint())
        assertEquals('e', scanner.peek())
        scanner.next()

        assertFalse(scanner.hasNext())
        assertEquals('e'.code, scanner.peekPreviousCodePoint())
        assertEquals('\u0000', scanner.peek())
    }

    @Test
    fun testCodePoints() {
        val scanner = Scanner(listOf(SourceLine.of("\uD83D\uDE0A", null)), 0, 0)

        assertTrue(scanner.hasNext())
        assertEquals('\u0000'.code, scanner.peekPreviousCodePoint())
        assertEquals(128522, scanner.peekCodePoint())
        scanner.next()
        // This jumps chars, not code points. So jump two here
        scanner.next()

        assertFalse(scanner.hasNext())
        assertEquals(128522, scanner.peekPreviousCodePoint())
        assertEquals('\u0000'.code, scanner.peekCodePoint())
    }

    @Test
    fun testTextBetween() {
        val scanner = Scanner(
            listOf(
                SourceLine.of("ab", SourceSpan.of(10, 3, 13, 2)),
                SourceLine.of("cde", SourceSpan.of(11, 4, 20, 3))
            ),
            0, 0
        )

        val start = scanner.position()

        scanner.next()
        assertSourceLines(
            scanner.getSource(start, scanner.position()),
            "a",
            SourceSpan.of(10, 3, 13, 1)
        )

        val afterA = scanner.position()

        scanner.next()
        assertSourceLines(
            scanner.getSource(start, scanner.position()),
            "ab",
            SourceSpan.of(10, 3, 13, 2)
        )

        val afterB = scanner.position()

        scanner.next()
        assertSourceLines(
            scanner.getSource(start, scanner.position()),
            "ab\n",
            SourceSpan.of(10, 3, 13, 2)
        )

        scanner.next()
        assertSourceLines(
            scanner.getSource(start, scanner.position()),
            "ab\nc",
            SourceSpan.of(10, 3, 13, 2),
            SourceSpan.of(11, 4, 20, 1)
        )

        scanner.next()
        assertSourceLines(
            scanner.getSource(start, scanner.position()),
            "ab\ncd",
            SourceSpan.of(10, 3, 13, 2),
            SourceSpan.of(11, 4, 20, 2)
        )

        scanner.next()
        assertSourceLines(
            scanner.getSource(start, scanner.position()),
            "ab\ncde",
            SourceSpan.of(10, 3, 13, 2),
            SourceSpan.of(11, 4, 20, 3)
        )

        assertSourceLines(
            scanner.getSource(afterA, scanner.position()),
            "b\ncde",
            SourceSpan.of(10, 4, 14, 1),
            SourceSpan.of(11, 4, 20, 3)
        )

        assertSourceLines(
            scanner.getSource(afterB, scanner.position()),
            "\ncde",
            SourceSpan.of(11, 4, 20, 3)
        )
    }

    private fun assertSourceLines(sourceLines: SourceLines, expectedContent: String, vararg expectedSourceSpans: SourceSpan) {
        assertEquals(expectedContent, sourceLines.getContent())
        assertEquals(listOf(*expectedSourceSpans), sourceLines.getSourceSpans())
    }

    @Test
    fun nextString() {
        val scanner = Scanner.of(
            SourceLines.of(
                listOf(
                    SourceLine.of("hey ya", null),
                    SourceLine.of("hi", null)
                )
            )
        )
        assertFalse(scanner.next("hoy"))
        assertTrue(scanner.next("hey"))
        assertTrue(scanner.next(' '))
        assertFalse(scanner.next("yo"))
        assertTrue(scanner.next("ya"))
        assertFalse(scanner.next(" "))
    }
}
