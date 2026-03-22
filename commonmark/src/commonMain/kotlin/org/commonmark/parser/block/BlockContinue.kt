package org.commonmark.parser.block

import org.commonmark.internal.BlockContinueImpl

/**
 * Result object for continuing parsing of a block, see static methods for constructors.
 */
public open class BlockContinue {

    public companion object {
        public fun none(): BlockContinue? = null

        public fun atIndex(newIndex: Int): BlockContinue = BlockContinueImpl(newIndex, -1, false)

        public fun atColumn(newColumn: Int): BlockContinue = BlockContinueImpl(-1, newColumn, false)

        public fun finished(): BlockContinue = BlockContinueImpl(-1, -1, true)
    }
}
