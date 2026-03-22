package org.commonmark.parser

import org.commonmark.node.*
import org.commonmark.parser.beta.InlineContentParser
import org.commonmark.parser.beta.InlineContentParserFactory
import org.commonmark.parser.beta.InlineParserState
import org.commonmark.parser.beta.ParsedInline
import org.commonmark.test.TestNodes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InlineContentParserTest {

    @Test
    fun customInlineContentParser() {
        val parser = Parser.builder().customInlineContentParserFactory(DollarInlineParser.Factory()).build()
        val doc = parser.parse("Test: \$hey *there*\$ \$you\$\n\n# Heading \$heading\$\n")
        val inline1 = TestNodes.find(doc, DollarInline::class)
        assertEquals("hey *there*", inline1.literal)

        val inline2 = doc.firstChild!!.lastChild as DollarInline
        assertEquals("you", inline2.literal)

        val heading = TestNodes.find(doc, Heading::class)
        val inline3 = heading.lastChild as DollarInline
        assertEquals("heading", inline3.literal)

        // Parser is created for each inline snippet, which is why the index resets for the second snippet.
        assertEquals(0, inline1.index)
        assertEquals(1, inline2.index)
        assertEquals(0, inline3.index)
    }

    @Test
    fun bangInlineContentParser() {
        // See if using ! for a custom inline content parser works.
        // ![] is used for images, but if it's not followed by a [, it should be possible to parse it differently.
        val parser = Parser.builder().customInlineContentParserFactory(BangInlineParser.Factory()).build()
        val doc = parser.parse("![image](url) !notimage")
        val image = TestNodes.find(doc, Image::class)
        assertEquals("url", image.destination)
        assertEquals(" ", (image.next as Text).literal)
        // Class
        assertTrue(image.next!!.next is BangInline)
        assertEquals("notimage", (image.next!!.next!!.next as Text).literal)
    }

    private class DollarInline(val literal: String, val index: Int) : CustomNode()

    private class DollarInlineParser : InlineContentParser {

        private var index = 0

        override fun tryParse(inlineParserState: InlineParserState): ParsedInline? {
            val scanner = inlineParserState.scanner()
            scanner.next()
            val pos = scanner.position()

            val end = scanner.find('$')
            if (end == -1) {
                return ParsedInline.none()
            }
            val content = scanner.getSource(pos, scanner.position()).getContent()
            scanner.next()
            return ParsedInline.of(DollarInline(content, index++), scanner.position())
        }

        class Factory : InlineContentParserFactory {
            override val triggerCharacters: Set<Char> = setOf('$')

            override fun create(): InlineContentParser {
                return DollarInlineParser()
            }
        }
    }

    private class BangInline : CustomNode()

    private class BangInlineParser : InlineContentParser {

        override fun tryParse(inlineParserState: InlineParserState): ParsedInline? {
            val scanner = inlineParserState.scanner()
            scanner.next()
            return ParsedInline.of(BangInline(), scanner.position())
        }

        class Factory : InlineContentParserFactory {
            override val triggerCharacters: Set<Char> = setOf('!')

            override fun create(): InlineContentParser {
                return BangInlineParser()
            }
        }
    }
}
