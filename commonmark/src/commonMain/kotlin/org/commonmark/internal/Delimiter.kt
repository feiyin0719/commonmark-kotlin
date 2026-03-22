package org.commonmark.internal

import org.commonmark.node.Text
import org.commonmark.parser.delimiter.DelimiterRun

/**
 * Delimiter (emphasis, strong emphasis or custom emphasis).
 */
internal class Delimiter(
    val characters: MutableList<Text>,
    val delimiterChar: Char,
    override val canOpen: Boolean,
    override val canClose: Boolean,
    var previous: Delimiter?
) : DelimiterRun {

    var next: Delimiter? = null

    override val originalLength: Int = characters.size

    override val length: Int
        get() = characters.size

    override val opener: Text
        get() = characters[characters.size - 1]

    override val closer: Text
        get() = characters[0]

    override fun getOpeners(length: Int): Iterable<Text> {
        require(length in 1..this.length) {
            "length must be between 1 and ${this.length}, was $length"
        }
        return characters.subList(characters.size - length, characters.size)
    }

    override fun getClosers(length: Int): Iterable<Text> {
        require(length in 1..this.length) {
            "length must be between 1 and ${this.length}, was $length"
        }
        return characters.subList(0, length)
    }
}
