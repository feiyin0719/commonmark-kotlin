package org.commonmark.renderer.html

import org.commonmark.internal.util.Escaping

/**
 * Writes HTML to a [StringBuilder].
 */
public class HtmlWriter(
    private val buffer: StringBuilder,
) {
    private var lastChar: Char = '\u0000'

    public fun raw(s: String) {
        append(s)
    }

    public fun text(text: String) {
        append(Escaping.escapeHtml(text))
    }

    public fun tag(name: String) {
        tag(name, emptyMap())
    }

    public fun tag(
        name: String,
        attrs: Map<String, String?>,
    ) {
        tag(name, attrs, false)
    }

    public fun tag(
        name: String,
        attrs: Map<String, String?>,
        voidElement: Boolean,
    ) {
        append("<")
        append(name)
        if (attrs.isNotEmpty()) {
            for ((key, value) in attrs) {
                append(" ")
                append(Escaping.escapeHtml(key))
                if (value != null) {
                    append("=\"")
                    append(Escaping.escapeHtml(value))
                    append("\"")
                }
            }
        }
        if (voidElement) {
            append(" /")
        }
        append(">")
    }

    public fun line() {
        if (lastChar != '\u0000' && lastChar != '\n') {
            append("\n")
        }
    }

    protected fun append(s: String) {
        buffer.append(s)
        val length = s.length
        if (length != 0) {
            lastChar = s[length - 1]
        }
    }
}
