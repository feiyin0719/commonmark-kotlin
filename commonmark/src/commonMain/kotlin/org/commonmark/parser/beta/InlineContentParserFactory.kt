package org.commonmark.parser.beta

/**
 * A factory for extending inline content parsing.
 */
public interface InlineContentParserFactory {
    public val triggerCharacters: Set<Char>

    public fun create(): InlineContentParser
}
