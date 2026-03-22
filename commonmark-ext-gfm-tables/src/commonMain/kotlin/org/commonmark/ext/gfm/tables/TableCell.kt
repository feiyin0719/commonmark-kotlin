package org.commonmark.ext.gfm.tables

import org.commonmark.node.CustomNode

/**
 * Table cell of a [TableRow] containing inline nodes.
 */
public class TableCell : CustomNode() {
    /**
     * Whether the cell is a header or not.
     */
    public var isHeader: Boolean = false

    /**
     * The cell alignment or `null` if no specific alignment.
     */
    public var alignment: Alignment? = null

    /**
     * The cell width (the number of dash and colon characters in the delimiter row of the table for this column).
     */
    public var width: Int = 0

    /**
     * How the cell is aligned horizontally.
     */
    public enum class Alignment {
        LEFT,
        CENTER,
        RIGHT,
    }
}
