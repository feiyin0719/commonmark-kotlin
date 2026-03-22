package org.commonmark.ext.front.matter

import org.commonmark.Extension
import org.commonmark.ext.front.matter.internal.YamlFrontMatterBlockParser
import org.commonmark.parser.Parser

/**
 * Extension for YAML-like metadata.
 *
 * Create it with [create] and then configure it on the builders
 * ([Parser.Builder.extensions]).
 *
 * The parsed metadata is turned into [YamlFrontMatterNode]. You can access the metadata using [YamlFrontMatterVisitor].
 */
public class YamlFrontMatterExtension private constructor() : Parser.ParserExtension {

    public companion object {
        @JvmStatic
        public fun create(): Extension = YamlFrontMatterExtension()
    }

    override fun extend(parserBuilder: Parser.Builder) {
        parserBuilder.customBlockParserFactory(YamlFrontMatterBlockParser.Factory())
    }
}
