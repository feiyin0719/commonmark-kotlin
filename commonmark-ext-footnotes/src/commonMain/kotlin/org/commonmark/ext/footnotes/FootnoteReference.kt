package org.commonmark.ext.footnotes

import org.commonmark.node.CustomNode

/**
 * A footnote reference, e.g. `[^foo]` in `Some text with a footnote[^foo]`
 *
 * The [label] is the text within brackets after `^`, so `foo` in the example. It needs to
 * match the label of a corresponding [FootnoteDefinition] for the footnote to be parsed.
 */
public class FootnoteReference(public val label: String) : CustomNode()
