package org.commonmark.parser.beta

import org.commonmark.node.Text

/**
 * A parsed link/image.
 */
public interface LinkInfo {
    /** The marker if present, or null (e.g. `!` for an image). */
    public val marker: Text?

    /** The text node of the opening bracket `[`. */
    public val openingBracket: Text

    /** The text between the first brackets. */
    public val text: String

    /** The label, or null for inline links or shortcut links. */
    public val label: String?

    /** The destination if available, or null. */
    public val destination: String?

    /** The title if available, or null. */
    public val title: String?

    /** The position after the closing text bracket. */
    public val afterTextBracket: Position
}
