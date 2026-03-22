package org.commonmark.parser

import org.commonmark.node.LinkReferenceDefinition
import org.commonmark.parser.beta.InlineContentParserFactory
import org.commonmark.parser.beta.LinkProcessor
import org.commonmark.parser.delimiter.DelimiterProcessor
import kotlin.reflect.KClass

/**
 * Context for inline parsing.
 */
public interface InlineParserContext {
    public val customInlineContentParserFactories: List<InlineContentParserFactory>
    public val customDelimiterProcessors: List<DelimiterProcessor>
    public val customLinkProcessors: List<LinkProcessor>
    public val customLinkMarkers: Set<Char>

    @Deprecated("use getDefinition with LinkReferenceDefinition instead")
    public fun getLinkReferenceDefinition(label: String): LinkReferenceDefinition?

    public fun <D : Any> getDefinition(
        type: KClass<D>,
        label: String,
    ): D?
}
