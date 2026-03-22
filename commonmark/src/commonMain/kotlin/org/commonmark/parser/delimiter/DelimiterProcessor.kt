package org.commonmark.parser.delimiter

import org.commonmark.node.Text

/**
 * Custom delimiter processor for additional delimiters besides `_` and `*`.
 */
public interface DelimiterProcessor {
    public val openingCharacter: Char
    public val closingCharacter: Char
    public val minLength: Int

    /**
     * Process the delimiter runs.
     *
     * @return how many delimiters were used; must not be greater than length of either opener or closer
     */
    public fun process(openingRun: DelimiterRun, closingRun: DelimiterRun): Int
}
