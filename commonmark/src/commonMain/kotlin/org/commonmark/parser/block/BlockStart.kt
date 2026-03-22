package org.commonmark.parser.block

import org.commonmark.internal.BlockStartImpl

/**
 * Result object for starting parsing of a block, see static methods for constructors.
 */
public abstract class BlockStart {

    /** Continue parsing at the specified index. */
    public abstract fun atIndex(newIndex: Int): BlockStart

    /** Continue parsing at the specified column (for tab handling). */
    public abstract fun atColumn(newColumn: Int): BlockStart

    @Deprecated("use replaceParagraphLines instead")
    public abstract fun replaceActiveBlockParser(): BlockStart

    /**
     * Replace a number of lines from the current paragraph with the new block.
     */
    public abstract fun replaceParagraphLines(lines: Int): BlockStart

    public companion object {
        public fun none(): BlockStart? = null

        public fun of(vararg blockParsers: BlockParser): BlockStart {
            return BlockStartImpl(blockParsers.toList())
        }
    }
}
