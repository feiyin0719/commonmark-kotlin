package org.commonmark.internal

import org.commonmark.internal.util.Parsing
import org.commonmark.node.Block
import org.commonmark.node.BlockQuote
import org.commonmark.parser.block.AbstractBlockParser
import org.commonmark.parser.block.AbstractBlockParserFactory
import org.commonmark.parser.block.BlockContinue
import org.commonmark.parser.block.BlockStart
import org.commonmark.parser.block.MatchedBlockParser
import org.commonmark.parser.block.ParserState
import org.commonmark.text.Characters

internal class BlockQuoteParser : AbstractBlockParser() {

    override val block: BlockQuote = BlockQuote()

    override val isContainer: Boolean get() = true

    override fun canContain(childBlock: Block): Boolean = true

    override fun tryContinue(parserState: ParserState): BlockContinue? {
        val nextNonSpace = parserState.nextNonSpaceIndex
        if (isMarker(parserState, nextNonSpace)) {
            var newColumn = parserState.column + parserState.indent + 1
            // optional following space or tab
            if (Characters.isSpaceOrTab(parserState.line.content, nextNonSpace + 1)) {
                newColumn++
            }
            return BlockContinue.atColumn(newColumn)
        } else {
            return BlockContinue.none()
        }
    }

    class Factory : AbstractBlockParserFactory() {
        override fun tryStart(state: ParserState, matchedBlockParser: MatchedBlockParser): BlockStart? {
            val nextNonSpace = state.nextNonSpaceIndex
            if (isMarker(state, nextNonSpace)) {
                var newColumn = state.column + state.indent + 1
                // optional following space or tab
                if (Characters.isSpaceOrTab(state.line.content, nextNonSpace + 1)) {
                    newColumn++
                }
                return BlockStart.of(BlockQuoteParser()).atColumn(newColumn)
            } else {
                return BlockStart.none()
            }
        }
    }

    companion object {
        private fun isMarker(state: ParserState, index: Int): Boolean {
            val line = state.line.content
            return state.indent < Parsing.CODE_BLOCK_INDENT && index < line.length && line[index] == '>'
        }
    }
}
