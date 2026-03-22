package org.commonmark.parser.block

import org.commonmark.parser.SourceLine

/**
 * State of the parser that is used in block parsers.
 */
public interface ParserState {
    /** The current source line being parsed (full line). */
    public val line: SourceLine

    /** The current index within the line (0-based). */
    public val index: Int

    /** The index of the next non-space character starting from [index] (may be the same) (0-based). */
    public val nextNonSpaceIndex: Int

    /** The current column within the line (0-based), accounting for tab stops. */
    public val column: Int

    /** The indentation in columns (either by spaces or tab stop of 4), starting from [column]. */
    public val indent: Int

    /** True if the current line is blank starting from the index. */
    public val isBlank: Boolean

    /** The deepest open block parser. */
    public val activeBlockParser: BlockParser
}
