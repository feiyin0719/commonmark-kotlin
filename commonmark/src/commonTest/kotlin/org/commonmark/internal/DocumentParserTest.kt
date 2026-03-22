package org.commonmark.internal

import org.commonmark.node.*
import org.commonmark.parser.block.BlockParserFactory
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DocumentParserTest {

    companion object {
        private val CORE_FACTORIES: List<BlockParserFactory> = listOf(
            BlockQuoteParser.Factory(),
            HeadingParser.Factory(),
            FencedCodeBlockParser.Factory(),
            HtmlBlockParser.Factory(),
            ThematicBreakParser.Factory(),
            ListBlockParser.Factory(),
            IndentedCodeBlockParser.Factory()
        )
    }

    @Test
    fun calculateBlockParserFactories_givenAFullListOfAllowedNodes_includesAllCoreFactories() {
        val customParserFactories = emptyList<BlockParserFactory>()
        val enabledBlockTypes: Set<KClass<out Block>> = setOf(
            BlockQuote::class, Heading::class, FencedCodeBlock::class,
            HtmlBlock::class, ThematicBreak::class, ListBlock::class, IndentedCodeBlock::class
        )

        val blockParserFactories = DocumentParser.calculateBlockParserFactories(customParserFactories, enabledBlockTypes)
        assertEquals(CORE_FACTORIES.size, blockParserFactories.size)

        for (factory in CORE_FACTORIES) {
            assertTrue(hasInstance(blockParserFactories, factory::class))
        }
    }

    @Test
    fun calculateBlockParserFactories_givenAListOfAllowedNodes_includesAssociatedFactories() {
        val customParserFactories = emptyList<BlockParserFactory>()
        val nodes = mutableSetOf<KClass<out Block>>()
        nodes.add(IndentedCodeBlock::class)

        val blockParserFactories = DocumentParser.calculateBlockParserFactories(customParserFactories, nodes)

        assertEquals(1, blockParserFactories.size)
        assertTrue(hasInstance(blockParserFactories, IndentedCodeBlockParser.Factory::class))
    }

    private fun hasInstance(blockParserFactories: List<BlockParserFactory>, factoryClass: KClass<out BlockParserFactory>): Boolean {
        return blockParserFactories.any { it::class == factoryClass }
    }
}
