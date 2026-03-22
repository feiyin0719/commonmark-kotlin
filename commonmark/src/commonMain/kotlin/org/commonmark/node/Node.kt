package org.commonmark.node

/**
 * The base class of all CommonMark AST nodes ([Block] and inlines).
 *
 * A node can have multiple children, and a parent (except for the root node).
 */
public abstract class Node {
    private var _parent: Node? = null
    private var _firstChild: Node? = null
    private var _lastChild: Node? = null
    private var _prev: Node? = null
    private var _next: Node? = null
    private var sourceSpans: MutableList<SourceSpan>? = null

    public abstract fun accept(visitor: Visitor)

    public var next: Node?
        get() = _next
        internal set(value) {
            _next = value
        }

    public var previous: Node?
        get() = _prev
        internal set(value) {
            _prev = value
        }

    public var firstChild: Node?
        get() = _firstChild
        internal set(value) {
            _firstChild = value
        }

    public var lastChild: Node?
        get() = _lastChild
        internal set(value) {
            _lastChild = value
        }

    public open var parent: Node?
        get() = _parent
        protected set(value) {
            _parent = value
        }

    internal fun setParentNode(parent: Node?) {
        this._parent = parent
    }

    public fun appendChild(child: Node) {
        child.unlink()
        child.setParentNode(this)
        if (this._lastChild != null) {
            this._lastChild!!._next = child
            child._prev = this._lastChild
            this._lastChild = child
        } else {
            this._firstChild = child
            this._lastChild = child
        }
    }

    public fun prependChild(child: Node) {
        child.unlink()
        child.setParentNode(this)
        if (this._firstChild != null) {
            this._firstChild!!._prev = child
            child._next = this._firstChild
            this._firstChild = child
        } else {
            this._firstChild = child
            this._lastChild = child
        }
    }

    public fun unlink() {
        if (this._prev != null) {
            this._prev!!._next = this._next
        } else if (this._parent != null) {
            this._parent!!._firstChild = this._next
        }
        if (this._next != null) {
            this._next!!._prev = this._prev
        } else if (this._parent != null) {
            this._parent!!._lastChild = this._prev
        }
        this._parent = null
        this._prev = null
        this._next = null
    }

    public fun insertAfter(sibling: Node) {
        sibling.unlink()
        sibling._next = this._next
        if (sibling._next != null) {
            sibling._next!!._prev = sibling
        }
        sibling._prev = this
        this._next = sibling
        sibling.setParentNode(this._parent)
        if (sibling._next == null && sibling._parent != null) {
            sibling._parent!!._lastChild = sibling
        }
    }

    public fun insertBefore(sibling: Node) {
        sibling.unlink()
        sibling._prev = this._prev
        if (sibling._prev != null) {
            sibling._prev!!._next = sibling
        }
        sibling._next = this
        this._prev = sibling
        sibling.setParentNode(this._parent)
        if (sibling._prev == null && sibling._parent != null) {
            sibling._parent!!._firstChild = sibling
        }
    }

    public fun getSourceSpans(): List<SourceSpan> = sourceSpans?.toList() ?: emptyList()

    public fun setSourceSpans(sourceSpans: List<SourceSpan>) {
        if (sourceSpans.isEmpty()) {
            this.sourceSpans = null
        } else {
            this.sourceSpans = sourceSpans.toMutableList()
        }
    }

    public fun addSourceSpan(sourceSpan: SourceSpan) {
        if (this.sourceSpans == null) {
            this.sourceSpans = mutableListOf()
        }
        this.sourceSpans!!.add(sourceSpan)
    }

    override fun toString(): String = "${this::class.simpleName}{${toStringAttributes()}}"

    protected open fun toStringAttributes(): String = ""
}
