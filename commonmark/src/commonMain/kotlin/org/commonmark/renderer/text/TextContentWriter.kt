package org.commonmark.renderer.text

/**
 * Writer for text content rendering.
 */
public class TextContentWriter(
    private val buffer: StringBuilder,
    private val lineBreakRendering: LineBreakRendering = LineBreakRendering.COMPACT,
) {
    private val prefixes: ArrayDeque<String> = ArrayDeque()
    private val tight: ArrayDeque<Boolean> = ArrayDeque()

    private var blockSeparator: String? = null
    private var lastChar: Char = '\u0000'

    public fun whitespace() {
        if (lastChar != '\u0000' && lastChar != ' ') {
            write(' ')
        }
    }

    public fun colon() {
        if (lastChar != '\u0000' && lastChar != ':') {
            write(':')
        }
    }

    public fun line() {
        append('\n')
        writePrefixes()
    }

    public fun block() {
        blockSeparator =
            when {
                lineBreakRendering == LineBreakRendering.STRIP -> " "
                lineBreakRendering == LineBreakRendering.COMPACT || isTight() -> "\n"
                else -> "\n\n"
            }
    }

    public fun resetBlock() {
        blockSeparator = null
    }

    public fun writeStripped(s: String) {
        write(s.replace(Regex("[\\r\\n\\s]+"), " "))
    }

    public fun write(s: String) {
        flushBlockSeparator()
        append(s)
    }

    public fun write(c: Char) {
        flushBlockSeparator()
        append(c)
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
        write(prefix)
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

    private fun isTight(): Boolean = tight.isNotEmpty() && tight.last()

    private fun writePrefixes() {
        for (prefix in prefixes) {
            append(prefix)
        }
    }

    /**
     * If a block separator has been enqueued with [block] but not yet written, write it now.
     */
    private fun flushBlockSeparator() {
        val separator = blockSeparator
        if (separator != null) {
            if (separator == "\n" || separator == "\n\n") {
                for (i in separator.indices) {
                    val sep = separator[i]
                    append(sep)
                    writePrefixes()
                }
            } else {
                append(separator)
            }
            blockSeparator = null
        }
    }

    private fun append(s: String) {
        buffer.append(s)
        val length = s.length
        if (length != 0) {
            lastChar = s[length - 1]
        }
    }

    private fun append(c: Char) {
        buffer.append(c)
        lastChar = c
    }
}
