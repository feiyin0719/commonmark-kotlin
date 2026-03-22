package org.commonmark.internal

import org.commonmark.parser.block.BlockParser
import org.commonmark.parser.block.BlockStart

internal class BlockStartImpl(
    val blockParsers: List<BlockParser>,
) : BlockStart() {
    var newIndex: Int = -1
        private set
    var newColumn: Int = -1
        private set
    var replaceParagraphLines: Int = 0
        private set

    override fun atIndex(newIndex: Int): BlockStart {
        this.newIndex = newIndex
        return this
    }

    override fun atColumn(newColumn: Int): BlockStart {
        this.newColumn = newColumn
        return this
    }

    @Deprecated("use replaceParagraphLines instead")
    override fun replaceActiveBlockParser(): BlockStart {
        this.replaceParagraphLines = Int.MAX_VALUE
        return this
    }

    override fun replaceParagraphLines(lines: Int): BlockStart {
        this.replaceParagraphLines = lines
        return this
    }
}
