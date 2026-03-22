package org.commonmark.internal

import org.commonmark.parser.block.BlockContinue

internal class BlockContinueImpl(
    val newIndex: Int,
    val newColumn: Int,
    val isFinalize: Boolean,
) : BlockContinue()
