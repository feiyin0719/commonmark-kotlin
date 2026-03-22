package org.commonmark.parser.block

import org.commonmark.node.Block
import org.commonmark.node.DefinitionMap
import org.commonmark.node.SourceSpan
import org.commonmark.parser.InlineParser
import org.commonmark.parser.SourceLine

/**
 * Parser for a specific block node.
 *
 * Implementations should subclass [AbstractBlockParser] instead of implementing this directly.
 */
public interface BlockParser {
    /** Return true if the block that is parsed is a container (contains other blocks), or false if it's a leaf. */
    public val isContainer: Boolean

    /** Return true if the block can have lazy continuation lines. */
    public val canHaveLazyContinuationLines: Boolean

    public fun canContain(childBlock: Block): Boolean

    public val block: Block

    public fun tryContinue(parserState: ParserState): BlockContinue?

    /** Add the part of a line that belongs to this block parser to parse. */
    public fun addLine(line: SourceLine)

    /** Add a source span of the currently parsed block. */
    public fun addSourceSpan(sourceSpan: SourceSpan)

    /** Return definitions parsed by this parser. */
    public fun getDefinitions(): List<DefinitionMap<*>>

    public fun closeBlock()

    public fun parseInlines(inlineParser: InlineParser)
}
