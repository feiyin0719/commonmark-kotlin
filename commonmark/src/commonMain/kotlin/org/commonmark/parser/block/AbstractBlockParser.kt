package org.commonmark.parser.block

import org.commonmark.node.Block
import org.commonmark.node.DefinitionMap
import org.commonmark.node.SourceSpan
import org.commonmark.parser.InlineParser
import org.commonmark.parser.SourceLine

public abstract class AbstractBlockParser : BlockParser {

    override val isContainer: Boolean get() = false

    override val canHaveLazyContinuationLines: Boolean get() = false

    override fun canContain(childBlock: Block): Boolean = false

    override fun addLine(line: SourceLine) {}

    override fun addSourceSpan(sourceSpan: SourceSpan) {
        block.addSourceSpan(sourceSpan)
    }

    override fun getDefinitions(): List<DefinitionMap<*>> = emptyList()

    override fun closeBlock() {}

    override fun parseInlines(inlineParser: InlineParser) {}
}
