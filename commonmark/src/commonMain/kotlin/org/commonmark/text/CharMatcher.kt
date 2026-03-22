package org.commonmark.text

/**
 * Matcher interface for [Char] values.
 *
 * Note that because this matches on [Char] values only (as opposed to code points),
 * this only operates on the level of code units and doesn't support supplementary characters.
 */
public fun interface CharMatcher {
    public fun matches(c: Char): Boolean
}
