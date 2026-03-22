package org.commonmark.parser.beta

/**
 * Position within a [Scanner].
 */
public class Position internal constructor(
    internal val lineIndex: Int,
    internal val index: Int,
)
