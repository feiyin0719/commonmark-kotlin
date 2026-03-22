package org.commonmark.parser.block

import org.commonmark.parser.SourceLines

/**
 * Open block parser that was last matched during the continue phase.
 */
public interface MatchedBlockParser {
    public val matchedBlockParser: BlockParser

    /**
     * Returns the current paragraph lines if the matched block is a paragraph.
     */
    public val paragraphLines: SourceLines
}
