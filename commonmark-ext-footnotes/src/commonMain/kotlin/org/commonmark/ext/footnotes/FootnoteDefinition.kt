package org.commonmark.ext.footnotes

import org.commonmark.node.CustomBlock

/**
 * A footnote definition, e.g.:
 * ```
 * [^foo]: This is the footnote text
 * ```
 * The [label] is the text in brackets after `^`, so `foo` in the example. The contents
 * of the footnote are child nodes of the definition, a [org.commonmark.node.Paragraph] in the example.
 *
 * Footnote definitions are parsed even if there's no corresponding [FootnoteReference].
 */
public class FootnoteDefinition(public val label: String) : CustomBlock()
