package org.commonmark.test

import org.commonmark.internal.InlineParserImpl
import org.commonmark.node.LinkReferenceDefinition
import org.commonmark.parser.InlineParser
import org.commonmark.parser.InlineParserContext
import org.commonmark.parser.InlineParserFactory
import org.commonmark.parser.Parser
import org.commonmark.parser.beta.InlineContentParserFactory
import org.commonmark.parser.beta.LinkProcessor
import org.commonmark.parser.delimiter.DelimiterProcessor
import org.commonmark.renderer.html.HtmlRenderer
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals

class InlineParserContextTest {

    @Test
    fun labelShouldBeOriginalNotNormalized() {
        val inlineParserFactory = CapturingInlineParserFactory()

        val parser = Parser.builder().inlineParserFactory(inlineParserFactory).build()
        val input = "[link with special label][FooBarBaz]\n\n[foobarbaz]: /url"

        val rendered = HtmlRenderer.builder().build().render(parser.parse(input))

        // Lookup should pass original label to context
        assertEquals(listOf("FooBarBaz"), inlineParserFactory.lookups)

        // Context should normalize label for finding reference
        assertEquals("<p><a href=\"/url\">link with special label</a></p>\n", rendered)
    }

    class CapturingInlineParserFactory : InlineParserFactory {

        val lookups = mutableListOf<String>()

        override fun create(inlineParserContext: InlineParserContext): InlineParser {
            val wrappedContext = object : InlineParserContext {
                override val customInlineContentParserFactories: List<InlineContentParserFactory>
                    get() = inlineParserContext.customInlineContentParserFactories

                override val customDelimiterProcessors: List<DelimiterProcessor>
                    get() = inlineParserContext.customDelimiterProcessors

                override val customLinkProcessors: List<LinkProcessor>
                    get() = inlineParserContext.customLinkProcessors

                override val customLinkMarkers: Set<Char>
                    get() = inlineParserContext.customLinkMarkers

                @Suppress("DEPRECATION")
                override fun getLinkReferenceDefinition(label: String): LinkReferenceDefinition? {
                    return getDefinition(LinkReferenceDefinition::class, label)
                }

                override fun <D : Any> getDefinition(type: KClass<D>, label: String): D? {
                    lookups.add(label)
                    return inlineParserContext.getDefinition(type, label)
                }
            }

            return InlineParserImpl(wrappedContext)
        }
    }
}
