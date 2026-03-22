package org.commonmark.ext.footnotes.internal

import org.commonmark.ext.footnotes.FootnoteDefinition
import org.commonmark.node.Block
import org.commonmark.node.DefinitionMap
import org.commonmark.parser.block.AbstractBlockParser
import org.commonmark.parser.block.BlockContinue
import org.commonmark.parser.block.BlockParserFactory
import org.commonmark.parser.block.BlockStart
import org.commonmark.parser.block.MatchedBlockParser
import org.commonmark.parser.block.ParserState
import org.commonmark.text.Characters

/**
 * Parser for a single [FootnoteDefinition] block.
 */
internal class FootnoteBlockParser(
    label: String,
) : AbstractBlockParser() {
    private val footnoteBlock = FootnoteDefinition(label)

    override val block: Block
        get() = footnoteBlock

    override val isContainer: Boolean
        get() = true

    override fun canContain(childBlock: Block): Boolean = true

    override fun tryContinue(parserState: ParserState): BlockContinue? {
        if (parserState.indent >= 4) {
            // It looks like content needs to be indented by 4 so that it's part of a footnote (instead of starting a new block).
            return BlockContinue.atColumn(4)
        } else if (parserState.isBlank) {
            // A blank line doesn't finish a footnote yet. If there's another line with indent >= 4 after it,
            // that should result in another paragraph in this footnote definition.
            return BlockContinue.atIndex(parserState.index)
        } else {
            // We're not continuing to give other block parsers a chance to interrupt this definition.
            // But if no other block parser applied (including another FootnotesBlockParser), we will
            // accept the line via lazy continuation (same as a block quote).
            return BlockContinue.none()
        }
    }

    override fun getDefinitions(): List<DefinitionMap<*>> {
        val map = DefinitionMap(FootnoteDefinition::class)
        map.putIfAbsent(footnoteBlock.label, footnoteBlock)
        return listOf(map)
    }

    class Factory : BlockParserFactory {
        override fun tryStart(
            state: ParserState,
            matchedBlockParser: MatchedBlockParser,
        ): BlockStart? {
            if (state.indent >= 4) {
                return BlockStart.none()
            }
            var index = state.nextNonSpaceIndex
            val content = state.line.content
            if (content[index] != '[' || index + 1 >= content.length) {
                return BlockStart.none()
            }
            index++
            if (content[index] != '^' || index + 1 >= content.length) {
                return BlockStart.none()
            }
            // Now at first label character (if any)
            index++
            val labelStart = index

            var i = labelStart
            while (i < content.length) {
                val c = content[i]
                when (c) {
                    ']' -> {
                        if (i > labelStart && i + 1 < content.length && content[i + 1] == ':') {
                            val label = content.subSequence(labelStart, i).toString()
                            // After the colon, any number of spaces is skipped (not part of the content)
                            val afterSpaces = Characters.skipSpaceTab(content, i + 2, content.length)
                            return BlockStart.of(FootnoteBlockParser(label)).atIndex(afterSpaces)
                        } else {
                            return BlockStart.none()
                        }
                    }

                    ' ', '\r', '\n', '\u0000', '\t' -> {
                        return BlockStart.none()
                    }
                }
                i++
            }

            return BlockStart.none()
        }
    }
}
