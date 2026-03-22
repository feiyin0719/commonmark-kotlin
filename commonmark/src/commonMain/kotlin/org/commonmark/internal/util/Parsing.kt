package org.commonmark.internal.util

internal object Parsing {
    const val CODE_BLOCK_INDENT: Int = 4

    fun columnsToNextTabStop(column: Int): Int {
        // Tab stop is 4
        return 4 - (column % 4)
    }
}
