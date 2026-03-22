package org.commonmark.internal

import org.commonmark.parser.SourceLines
import org.commonmark.parser.block.BlockParser
import org.commonmark.parser.block.MatchedBlockParser

internal class MatchedBlockParserImpl(
    override val matchedBlockParser: BlockParser
) : MatchedBlockParser {

    override val paragraphLines: SourceLines
        get() {
            if (matchedBlockParser is ParagraphParser) {
                return matchedBlockParser.paragraphLines
            }
            return SourceLines.empty()
        }
}
