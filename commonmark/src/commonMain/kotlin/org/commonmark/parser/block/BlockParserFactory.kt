package org.commonmark.parser.block

/**
 * Parser factory for a block node for determining when a block starts.
 *
 * Implementations should subclass [AbstractBlockParserFactory] instead of implementing this directly.
 */
public interface BlockParserFactory {
    public fun tryStart(
        state: ParserState,
        matchedBlockParser: MatchedBlockParser,
    ): BlockStart?
}
