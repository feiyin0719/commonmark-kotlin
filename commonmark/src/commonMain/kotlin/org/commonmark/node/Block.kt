package org.commonmark.node

/**
 * Block nodes are nodes that form the structure of a document (as opposed to inline nodes which are used
 * for text content within blocks). The parent of a block is always another block.
 */
public abstract class Block : Node() {

    /**
     * Returns the parent block, or null if this is the root.
     */
    public val blockParent: Block?
        get() = parent as? Block
}
