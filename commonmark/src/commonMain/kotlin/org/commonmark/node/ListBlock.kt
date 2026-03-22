package org.commonmark.node

/**
 * Base class for [BulletList] and [OrderedList].
 */
public abstract class ListBlock : Block() {
    public var isTight: Boolean = false
}
