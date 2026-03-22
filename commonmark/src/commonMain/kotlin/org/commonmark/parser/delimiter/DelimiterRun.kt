package org.commonmark.parser.delimiter

import org.commonmark.node.Text

/**
 * A delimiter run is one or more of the same delimiter character, e.g. `***`.
 */
public interface DelimiterRun {
    public val canOpen: Boolean
    public val canClose: Boolean
    public val length: Int
    public val originalLength: Int
    public val opener: Text
    public val closer: Text

    public fun getOpeners(length: Int): Iterable<Text>

    public fun getClosers(length: Int): Iterable<Text>
}
