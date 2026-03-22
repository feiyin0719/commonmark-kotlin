package org.commonmark.parser.beta

/**
 * Parser for a type of inline content.
 */
public interface InlineContentParser {
    public fun tryParse(inlineParserState: InlineParserState): ParsedInline?
}
