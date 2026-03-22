package org.commonmark.renderer.markdown

import org.commonmark.text.CharMatcher

/**
 * Writer for Markdown (CommonMark) text.
 */
public class MarkdownWriter(
    private val buffer: StringBuilder,
) {
    private var blockSeparator: Int = 0
    private var lastChar: Char = '\u0000'
    private var atLineStart: Boolean = true

    // Stacks of settings that affect various rendering behaviors. The common pattern here is that callers use "push" to
    // change a setting, render some nodes, and then "pop" the setting off the stack again to restore previous state.
    private val prefixes: ArrayDeque<String> = ArrayDeque()
    private val tight: ArrayDeque<Boolean> = ArrayDeque()
    private val rawEscapes: ArrayDeque<CharMatcher> = ArrayDeque()

    /**
     * Write the supplied string (raw/unescaped except if [pushRawEscape] was used).
     */
    public fun raw(s: String) {
        flushBlockSeparator()
        write(s, null)
    }

    /**
     * Write the supplied character (raw/unescaped except if [pushRawEscape] was used).
     */
    public fun raw(c: Char) {
        flushBlockSeparator()
        write(c)
    }

    /**
     * Write the supplied string with escaping.
     *
     * @param s      the string to write
     * @param escape which characters to escape
     */
    public fun text(
        s: String,
        escape: CharMatcher,
    ) {
        if (s.isEmpty()) {
            return
        }
        flushBlockSeparator()
        write(s, escape)

        lastChar = s[s.length - 1]
        atLineStart = false
    }

    /**
     * Write a newline (line terminator).
     */
    public fun line() {
        write('\n')
        writePrefixes()
        atLineStart = true
    }

    /**
     * Enqueue a block separator to be written before the next text is written. Block separators are not written
     * straight away because if there are no more blocks to write we don't want a separator (at the end of the document).
     */
    public fun block() {
        // Remember whether this should be a tight or loose separator now because tight could get changed in between
        // this and the next flush.
        blockSeparator = if (isTight()) 1 else 2
        atLineStart = true
    }

    /**
     * Push a prefix onto the top of the stack. All prefixes are written at the beginning of each line, until the
     * prefix is popped again.
     *
     * @param prefix the raw prefix string
     */
    public fun pushPrefix(prefix: String) {
        prefixes.addLast(prefix)
    }

    /**
     * Write a prefix.
     *
     * @param prefix the raw prefix string to write
     */
    public fun writePrefix(prefix: String) {
        val tmp = atLineStart
        raw(prefix)
        atLineStart = tmp
    }

    /**
     * Remove the last prefix from the top of the stack.
     */
    public fun popPrefix() {
        prefixes.removeLast()
    }

    /**
     * Change whether blocks are tight or loose. Loose is the default where blocks are separated by a blank line. Tight
     * is where blocks are not separated by a blank line. Tight blocks are used in lists, if there are no blank lines
     * within the list.
     *
     * Note that changing this does not affect block separators that have already been enqueued with [block],
     * only future ones.
     */
    public fun pushTight(tight: Boolean) {
        this.tight.addLast(tight)
    }

    /**
     * Remove the last "tight" setting from the top of the stack.
     */
    public fun popTight() {
        this.tight.removeLast()
    }

    /**
     * Escape the characters matching the supplied matcher, in all text (text and raw). This might be useful to
     * extensions that add another layer of syntax, e.g. the tables extension that uses `|` to separate cells and needs
     * all `|` characters to be escaped (even in code spans).
     *
     * @param rawEscape the characters to escape in raw text
     */
    public fun pushRawEscape(rawEscape: CharMatcher) {
        rawEscapes.addLast(rawEscape)
    }

    /**
     * Remove the last raw escape from the top of the stack.
     */
    public fun popRawEscape() {
        rawEscapes.removeLast()
    }

    /**
     * @return the last character that was written
     */
    public fun getLastChar(): Char = lastChar

    /**
     * @return whether we're at the line start (not counting any prefixes), i.e. after a [line] or [block].
     */
    public fun isAtLineStart(): Boolean = atLineStart

    private fun write(
        s: String,
        escape: CharMatcher?,
    ) {
        if (rawEscapes.isEmpty() && escape == null) {
            // Normal fast path
            buffer.append(s)
        } else {
            for (i in s.indices) {
                append(s[i], escape)
            }
        }

        val length = s.length
        if (length != 0) {
            lastChar = s[length - 1]
        }
        atLineStart = false
    }

    private fun write(c: Char) {
        append(c, null)
        lastChar = c
        atLineStart = false
    }

    private fun writePrefixes() {
        if (prefixes.isNotEmpty()) {
            for (prefix in prefixes) {
                write(prefix, null)
            }
        }
    }

    /**
     * If a block separator has been enqueued with [block] but not yet written, write it now.
     */
    private fun flushBlockSeparator() {
        if (blockSeparator != 0) {
            write('\n')
            writePrefixes()
            if (blockSeparator > 1) {
                write('\n')
                writePrefixes()
            }
            blockSeparator = 0
        }
    }

    private fun append(
        c: Char,
        escape: CharMatcher?,
    ) {
        if (needsEscaping(c, escape)) {
            if (c == '\n') {
                // Can't escape this with \, use numeric character reference
                buffer.append("&#10;")
            } else {
                buffer.append('\\')
                buffer.append(c)
            }
        } else {
            buffer.append(c)
        }
    }

    private fun isTight(): Boolean = tight.isNotEmpty() && tight.last()

    private fun needsEscaping(
        c: Char,
        escape: CharMatcher?,
    ): Boolean = (escape != null && escape.matches(c)) || rawNeedsEscaping(c)

    private fun rawNeedsEscaping(c: Char): Boolean {
        for (rawEscape in rawEscapes) {
            if (rawEscape.matches(c)) {
                return true
            }
        }
        return false
    }
}
