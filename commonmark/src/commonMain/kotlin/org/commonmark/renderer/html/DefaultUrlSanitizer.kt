package org.commonmark.renderer.html

/**
 * Allows http, https, mailto, and data protocols for url.
 * Also allows protocol relative urls, and relative urls.
 * Implementation based on https://github.com/OWASP/java-html-sanitizer
 */
public class DefaultUrlSanitizer : UrlSanitizer {
    private val protocols: Set<String>

    public constructor() : this(listOf("http", "https", "mailto", "data"))

    public constructor(protocols: Collection<String>) {
        this.protocols = protocols.toHashSet()
    }

    override fun sanitizeLinkUrl(url: String): String {
        val stripped = stripHtmlSpaces(url)
        for (i in stripped.indices) {
            when (stripped[i]) {
                '/', '#', '?' -> {
                    // No protocol.
                    return stripped
                }

                ':' -> {
                    val protocol = stripped.substring(0, i).lowercase()
                    return if (!protocols.contains(protocol)) {
                        ""
                    } else {
                        stripped
                    }
                }
            }
        }
        return stripped
    }

    override fun sanitizeImageUrl(url: String): String = sanitizeLinkUrl(url)

    private fun stripHtmlSpaces(s: String): String {
        var n = s.length
        var i = 0
        while (n > i) {
            if (!isHtmlSpace(s[n - 1])) {
                break
            }
            n--
        }
        while (i < n) {
            if (!isHtmlSpace(s[i])) {
                break
            }
            i++
        }
        return if (i == 0 && n == s.length) {
            s
        } else {
            s.substring(i, n)
        }
    }

    private fun isHtmlSpace(ch: Char): Boolean =
        when (ch) {
            ' ', '\t', '\n', '\u000c', '\r' -> true
            else -> false
        }
}
