package org.commonmark.test

import org.commonmark.node.Block
import org.commonmark.node.CustomBlock
import org.commonmark.node.Paragraph
import org.commonmark.node.SourceSpan
import org.commonmark.node.Text
import org.commonmark.parser.IncludeSourceSpans
import org.commonmark.parser.InlineParser
import org.commonmark.parser.Parser
import org.commonmark.parser.SourceLines
import org.commonmark.parser.block.AbstractBlockParser
import org.commonmark.parser.block.AbstractBlockParserFactory
import org.commonmark.parser.block.BlockContinue
import org.commonmark.parser.block.BlockStart
import org.commonmark.parser.block.MatchedBlockParser
import org.commonmark.parser.block.ParserState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BlockParserFactoryTest {
    @Test
    fun customBlockParserFactory() {
        val parser = Parser.builder().customBlockParserFactory(DashBlockParser.Factory()).build()

        // The dashes would normally be a ThematicBreak
        val doc = parser.parse("hey\n\n---\n")

        assertTrue(doc.firstChild is Paragraph)
        assertEquals("hey", (doc.firstChild!!.firstChild as Text).literal)
        assertTrue(doc.lastChild is DashBlock)
    }

    @Test
    @Suppress("DEPRECATION")
    fun replaceActiveBlockParser() {
        val parser =
            Parser
                .builder()
                .customBlockParserFactory(StarHeadingBlockParser.Factory())
                .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
                .build()

        val doc = parser.parse("a\nbc\n***\n")

        val heading = doc.firstChild
        assertTrue(heading is StarHeading)
        assertNull(heading!!.next)
        val a = heading.firstChild
        assertTrue(a is Text)
        assertEquals("a", (a as Text).literal)
        val bc = a.next!!.next
        assertTrue(bc is Text)
        assertEquals("bc", (bc as Text).literal)
        assertNull(bc!!.next)

        assertEquals(
            listOf(
                SourceSpan.of(0, 0, 0, 1),
                SourceSpan.of(1, 0, 2, 2),
                SourceSpan.of(2, 0, 5, 3),
            ),
            heading.getSourceSpans(),
        )
        assertEquals(listOf(SourceSpan.of(0, 0, 0, 1)), a.getSourceSpans())
        assertEquals(listOf(SourceSpan.of(1, 0, 2, 2)), bc.getSourceSpans())
    }

    private class DashBlock : CustomBlock()

    private class DashBlockParser : AbstractBlockParser() {
        private val dash = DashBlock()

        override val block: Block get() = dash

        override fun tryContinue(parserState: ParserState): BlockContinue? = null

        class Factory : AbstractBlockParserFactory() {
            override fun tryStart(
                state: ParserState,
                matchedBlockParser: MatchedBlockParser,
            ): BlockStart? {
                if (state.line.content.toString() == "---") {
                    return BlockStart.of(DashBlockParser())
                }
                return null
            }
        }
    }

    private class StarHeading : CustomBlock()

    private class StarHeadingBlockParser(
        private val content: SourceLines,
    ) : AbstractBlockParser() {
        private val heading = StarHeading()

        override val block: Block get() = heading

        override fun tryContinue(parserState: ParserState): BlockContinue? = null

        override fun parseInlines(inlineParser: InlineParser) {
            inlineParser.parse(content, heading)
        }

        class Factory : AbstractBlockParserFactory() {
            @Suppress("DEPRECATION")
            override fun tryStart(
                state: ParserState,
                matchedBlockParser: MatchedBlockParser,
            ): BlockStart? {
                val lines = matchedBlockParser.paragraphLines
                if (state.line.content
                        .toString()
                        .startsWith("***")
                ) {
                    return BlockStart
                        .of(StarHeadingBlockParser(lines))
                        .replaceActiveBlockParser()
                } else {
                    return null
                }
            }
        }
    }
}
