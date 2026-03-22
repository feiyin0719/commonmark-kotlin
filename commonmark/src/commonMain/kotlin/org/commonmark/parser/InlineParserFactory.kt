package org.commonmark.parser

/**
 * Factory for custom inline parser.
 */
public fun interface InlineParserFactory {
    /**
     * Create an [InlineParser] to use for parsing inlines. This is called once per parsed document.
     */
    public fun create(inlineParserContext: InlineParserContext): InlineParser
}
