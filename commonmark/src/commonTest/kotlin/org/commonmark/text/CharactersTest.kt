package org.commonmark.text

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CharactersTest {

    @Test
    fun isPunctuation() {
        // From https://spec.commonmark.org/0.29/#ascii-punctuation-character
        val chars = charArrayOf(
            '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', // (U+0021-2F)
            ':', ';', '<', '=', '>', '?', '@', // (U+003A-0040)
            '[', '\\', ']', '^', '_', '`', // (U+005B-0060)
            '{', '|', '}', '~' // (U+007B-007E)
        )

        for (c in chars) {
            assertTrue(Characters.isPunctuationCodePoint(c.code), "Expected to be punctuation: $c")
        }
    }

    @Test
    fun isBlank() {
        assertTrue(Characters.isBlank(""))
        assertTrue(Characters.isBlank(" "))
        assertTrue(Characters.isBlank("\t"))
        assertTrue(Characters.isBlank(" \t"))
        assertFalse(Characters.isBlank("a"))
        assertFalse(Characters.isBlank("\u000C"))
    }
}
