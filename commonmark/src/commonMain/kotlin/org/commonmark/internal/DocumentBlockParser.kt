package org.commonmark.internal

import org.commonmark.node.Block
import org.commonmark.node.Document
import org.commonmark.parser.SourceLine
import org.commonmark.parser.block.AbstractBlockParser
import org.commonmark.parser.block.BlockContinue
import org.commonmark.parser.block.ParserState

internal class DocumentBlockParser : AbstractBlockParser() {

    override val block: Document = Document()

    override val isContainer: Boolean get() = true

    override fun canContain(childBlock: Block): Boolean = true

    override fun tryContinue(parserState: ParserState): BlockContinue? {
        return BlockContinue.atIndex(parserState.index)
    }

    override fun addLine(line: SourceLine) {
    }
}
