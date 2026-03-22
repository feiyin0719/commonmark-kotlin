package org.commonmark.ext.ins

import org.commonmark.node.CustomNode
import org.commonmark.node.Delimited

/**
 * An ins node containing text and other inline nodes as children.
 */
public class Ins :
    CustomNode(),
    Delimited {
    override val openingDelimiter: String = "++"

    override val closingDelimiter: String = "++"
}
