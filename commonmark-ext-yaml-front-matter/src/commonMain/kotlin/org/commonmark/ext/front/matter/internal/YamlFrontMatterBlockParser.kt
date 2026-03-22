package org.commonmark.ext.front.matter.internal

import org.commonmark.ext.front.matter.YamlFrontMatterBlock
import org.commonmark.ext.front.matter.YamlFrontMatterNode
import org.commonmark.node.Block
import org.commonmark.node.Document
import org.commonmark.parser.InlineParser
import org.commonmark.parser.SourceLine
import org.commonmark.parser.block.*

internal class YamlFrontMatterBlockParser : AbstractBlockParser() {
    private var inLiteral = false
    private var currentKey: String? = null
    private var currentValues = mutableListOf<String>()
    private val yamlBlock = YamlFrontMatterBlock()

    override val block: Block get() = yamlBlock

    override fun addLine(line: SourceLine) {
    }

    override fun tryContinue(parserState: ParserState): BlockContinue? {
        val line = parserState.line.content

        if (REGEX_END.matches(line)) {
            if (currentKey != null) {
                yamlBlock.appendChild(YamlFrontMatterNode(currentKey!!, currentValues))
            }
            return BlockContinue.finished()
        }

        val metadataMatch = REGEX_METADATA.matchEntire(line)
        if (metadataMatch != null) {
            if (currentKey != null) {
                yamlBlock.appendChild(YamlFrontMatterNode(currentKey!!, currentValues))
            }

            inLiteral = false
            currentKey = metadataMatch.groupValues[1]
            currentValues = mutableListOf()
            val value = metadataMatch.groupValues[2]
            if (value == "|") {
                inLiteral = true
            } else if (value.isNotEmpty()) {
                currentValues.add(parseString(value))
            }

            return BlockContinue.atIndex(parserState.index)
        } else {
            if (inLiteral) {
                val literalMatch = REGEX_METADATA_LITERAL.matchEntire(line)
                if (literalMatch != null) {
                    if (currentValues.size == 1) {
                        currentValues[0] = currentValues[0] + "\n" + literalMatch.groupValues[1].trim()
                    } else {
                        currentValues.add(literalMatch.groupValues[1].trim())
                    }
                }
            } else {
                val listMatch = REGEX_METADATA_LIST.matchEntire(line)
                if (listMatch != null) {
                    val value = listMatch.groupValues[1]
                    currentValues.add(parseString(value))
                }
            }

            return BlockContinue.atIndex(parserState.index)
        }
    }

    override fun parseInlines(inlineParser: InlineParser) {
    }

    class Factory : AbstractBlockParserFactory() {
        override fun tryStart(
            state: ParserState,
            matchedBlockParser: MatchedBlockParser,
        ): BlockStart? {
            val line = state.line.content
            val parentParser = matchedBlockParser.matchedBlockParser
            // check whether this line is the first line of whole document or not
            if (parentParser.block is Document && parentParser.block.firstChild == null &&
                REGEX_BEGIN.matches(line)
            ) {
                return BlockStart.of(YamlFrontMatterBlockParser()).atIndex(state.nextNonSpaceIndex)
            }

            return BlockStart.none()
        }
    }

    companion object {
        private val REGEX_METADATA = Regex("^[ ]{0,3}([A-Za-z0-9._-]+):\\s*(.*)")
        private val REGEX_METADATA_LIST = Regex("^[ ]+-\\s*(.*)")
        private val REGEX_METADATA_LITERAL = Regex("^\\s*(.*)")
        private val REGEX_BEGIN = Regex("^-{3}(\\s.*)?")
        private val REGEX_END = Regex("^(-{3}|\\.{3})(\\s.*)?")

        private fun parseString(s: String): String {
            if (s.startsWith("'") && s.endsWith("'")) {
                val inner = s.substring(1, s.length - 1)
                return inner.replace("''", "'")
            } else if (s.startsWith("\"") && s.endsWith("\"")) {
                val inner = s.substring(1, s.length - 1)
                return inner
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
            } else {
                return s
            }
        }
    }
}
