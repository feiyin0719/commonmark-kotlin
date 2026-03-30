package org.commonmark.ext.htmlconverter.internal

/**
 * Lightweight HTML tokenizer for Kotlin Multiplatform.
 * Produces a sequence of [HtmlToken] from raw HTML text.
 */
internal class HtmlTokenizer(
    private val html: String,
) {
    private var pos = 0

    fun tokenize(): List<HtmlToken> {
        val tokens = mutableListOf<HtmlToken>()
        while (pos < html.length) {
            if (html[pos] == '<') {
                val token = readTag()
                if (token != null) {
                    tokens.add(token)
                }
            } else {
                val text = readText()
                if (text.isNotEmpty()) {
                    tokens.add(HtmlToken.Text(text))
                }
            }
        }
        return tokens
    }

    private fun readText(): String {
        val start = pos
        while (pos < html.length && html[pos] != '<') {
            pos++
        }
        return decodeHtmlEntities(html.substring(start, pos))
    }

    private fun readTag(): HtmlToken? {
        val start = pos
        pos++ // skip '<'

        if (pos < html.length && html[pos] == '!') {
            // Comment or doctype
            if (html.startsWith("<!--", start)) {
                val end = html.indexOf("-->", pos)
                pos = if (end != -1) end + 3 else html.length
                return null // skip comments
            }
            // DOCTYPE or other declarations
            val end = html.indexOf('>', pos)
            pos = if (end != -1) end + 1 else html.length
            return null
        }

        val isClosing = pos < html.length && html[pos] == '/'
        if (isClosing) pos++

        // Read tag name
        val nameStart = pos
        while (pos < html.length && html[pos] != '>' && html[pos] != ' ' && html[pos] != '/' && html[pos] != '\t' && html[pos] != '\n' && html[pos] != '\r') {
            pos++
        }
        val tagName = html.substring(nameStart, pos).lowercase()

        if (tagName.isEmpty()) {
            // Not a valid tag, treat as text
            pos = start + 1
            return HtmlToken.Text("<")
        }

        // Read attributes
        val attributes = mutableMapOf<String, String>()
        skipWhitespace()
        while (pos < html.length && html[pos] != '>' && html[pos] != '/') {
            val attr = readAttribute()
            if (attr != null) {
                attributes[attr.first.lowercase()] = attr.second
            }
            skipWhitespace()
        }

        val isSelfClosing = pos < html.length && html[pos] == '/'
        if (isSelfClosing) pos++

        if (pos < html.length && html[pos] == '>') {
            pos++
        }

        return if (isClosing) {
            HtmlToken.CloseTag(tagName)
        } else {
            HtmlToken.OpenTag(tagName, attributes, isSelfClosing || tagName in VOID_ELEMENTS)
        }
    }

    private fun readAttribute(): Pair<String, String>? {
        val nameStart = pos
        while (pos < html.length && html[pos] != '=' && html[pos] != '>' && html[pos] != ' ' && html[pos] != '/' && html[pos] != '\t' && html[pos] != '\n') {
            pos++
        }
        val name = html.substring(nameStart, pos)
        if (name.isEmpty()) {
            if (pos < html.length && html[pos] != '>' && html[pos] != '/') pos++
            return null
        }

        skipWhitespace()
        if (pos >= html.length || html[pos] != '=') {
            return name to ""
        }
        pos++ // skip '='
        skipWhitespace()

        val value =
            if (pos < html.length && (html[pos] == '"' || html[pos] == '\'')) {
                val quote = html[pos]
                pos++
                val valStart = pos
                while (pos < html.length && html[pos] != quote) {
                    pos++
                }
                val v = html.substring(valStart, pos)
                if (pos < html.length) pos++ // skip closing quote
                decodeHtmlEntities(v)
            } else {
                val valStart = pos
                while (pos < html.length && html[pos] != ' ' && html[pos] != '>' && html[pos] != '/') {
                    pos++
                }
                decodeHtmlEntities(html.substring(valStart, pos))
            }

        return name to value
    }

    private fun skipWhitespace() {
        while (pos < html.length && html[pos] in " \t\n\r") {
            pos++
        }
    }

    companion object {
        val VOID_ELEMENTS =
            setOf(
                "area", "base", "br", "col", "embed", "hr", "img", "input",
                "link", "meta", "param", "source", "track", "wbr",
            )

        fun decodeHtmlEntities(text: String): String {
            if ('&' !in text) return text

            val sb = StringBuilder(text.length)
            var i = 0
            while (i < text.length) {
                if (text[i] == '&') {
                    val semicolonIdx = text.indexOf(';', i + 1)
                    if (semicolonIdx != -1 && semicolonIdx - i <= 10) {
                        val entity = text.substring(i, semicolonIdx + 1)
                        val decoded = decodeEntity(entity)
                        if (decoded != null) {
                            sb.append(decoded)
                            i = semicolonIdx + 1
                            continue
                        }
                    }
                }
                sb.append(text[i])
                i++
            }
            return sb.toString()
        }

        private fun decodeEntity(entity: String): String? =
            when (entity) {
                "&amp;" -> "&"
                "&lt;" -> "<"
                "&gt;" -> ">"
                "&quot;" -> "\""
                "&apos;" -> "'"
                "&#39;" -> "'"
                "&nbsp;" -> "\u00A0"
                "&ndash;" -> "\u2013"
                "&mdash;" -> "\u2014"
                "&lsquo;" -> "\u2018"
                "&rsquo;" -> "\u2019"
                "&ldquo;" -> "\u201C"
                "&rdquo;" -> "\u201D"
                "&bull;" -> "\u2022"
                "&hellip;" -> "\u2026"
                "&copy;" -> "\u00A9"
                "&reg;" -> "\u00AE"
                "&trade;" -> "\u2122"
                "&times;" -> "\u00D7"
                "&divide;" -> "\u00F7"
                "&deg;" -> "\u00B0"
                "&para;" -> "\u00B6"
                "&sect;" -> "\u00A7"
                "&#10;" -> "\n"
                "&#13;" -> "\r"
                "&#9;" -> "\t"
                else -> {
                    if (entity.startsWith("&#x") && entity.endsWith(";")) {
                        val hex = entity.substring(3, entity.length - 1)
                        hex.toIntOrNull(16)?.toChar()?.toString()
                    } else if (entity.startsWith("&#") && entity.endsWith(";")) {
                        val num = entity.substring(2, entity.length - 1)
                        num.toIntOrNull()?.toChar()?.toString()
                    } else {
                        null
                    }
                }
            }
    }
}

internal sealed class HtmlToken {
    data class Text(val content: String) : HtmlToken()

    data class OpenTag(
        val name: String,
        val attributes: Map<String, String> = emptyMap(),
        val selfClosing: Boolean = false,
    ) : HtmlToken()

    data class CloseTag(val name: String) : HtmlToken()
}
