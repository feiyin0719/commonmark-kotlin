package org.commonmark.ext.autolink.internal

import org.commonmark.ext.autolink.AutolinkType

/**
 * Internal link type used by the link extractor to distinguish between URL schemes.
 */
internal enum class LinkType {
    URL,
    EMAIL,
    WWW
}

/**
 * A span within the input text. Base class for both plain text spans and link spans.
 */
internal open class Span(
    val beginIndex: Int,
    val endIndex: Int
)

/**
 * A span that represents a detected link within the input text.
 */
internal class LinkSpan(
    beginIndex: Int,
    endIndex: Int,
    val type: LinkType
) : Span(beginIndex, endIndex)

/**
 * Pure Kotlin link extractor that detects URLs and email addresses in plain text using regular
 * expressions. This replaces the JVM-only `org.nibor.autolink` library for Kotlin Multiplatform
 * compatibility.
 *
 * The extractor returns a list of [Span] objects that cover the entire input string. Each span is
 * either a plain [Span] (for non-link text) or a [LinkSpan] (for detected links).
 */
internal class LinkExtractor(private val linkTypes: Set<AutolinkType>) {

    companion object {
        // Matches URLs starting with http:// or https://
        // Captures the scheme and everything after it that looks like a URL (non-whitespace,
        // non-angle-bracket characters).
        private val URL_REGEX = Regex("""https?://[^\s<>]*[^\s<>]""")

        // Matches www. prefixed URLs (will be treated as http:// URLs).
        private val WWW_REGEX = Regex("""www\.[^\s<>]+""")

        // Matches email addresses: local-part@domain
        // Local part: alphanumeric plus . _ % + -
        // Domain: labels separated by dots, TLD at least 2 chars
        private val EMAIL_REGEX =
            Regex("""[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9](?:[a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?)+""")

        // Characters that should be stripped from the end of a matched link.
        private val TRAILING_PUNCTUATION = charArrayOf(
            '.', ',', ':', ';', '!', '?', '"', '\'', '*', '_', '~'
        )
    }

    /**
     * Extracts all spans from the given [input] text. The returned list of spans covers the entire
     * input string without gaps or overlaps. Each span is either a plain [Span] for non-link text
     * or a [LinkSpan] for a detected link.
     *
     * If no links are found, a single [Span] covering the entire input is returned.
     */
    fun extractSpans(input: String): List<Span> {
        if (input.isEmpty()) {
            return listOf(Span(0, 0))
        }

        val linkMatches = mutableListOf<LinkSpan>()

        if (AutolinkType.URL in linkTypes) {
            findLinks(input, URL_REGEX, LinkType.URL, linkMatches)
        }

        if (AutolinkType.WWW in linkTypes) {
            findLinks(input, WWW_REGEX, LinkType.WWW, linkMatches)
        }

        if (AutolinkType.EMAIL in linkTypes) {
            findLinks(input, EMAIL_REGEX, LinkType.EMAIL, linkMatches)
        }

        // Sort by start position; for ties, prefer URL over WWW over EMAIL
        linkMatches.sortWith(compareBy<LinkSpan> { it.beginIndex }.thenBy { it.type.ordinal })

        // Remove overlapping matches (keep the first one at each position)
        val filtered = mutableListOf<LinkSpan>()
        for (match in linkMatches) {
            if (filtered.isEmpty() || match.beginIndex >= filtered.last().endIndex) {
                filtered.add(match)
            }
        }

        // Build the full span list with text spans filling the gaps between links
        val result = mutableListOf<Span>()
        var pos = 0
        for (link in filtered) {
            if (pos < link.beginIndex) {
                result.add(Span(pos, link.beginIndex))
            }
            result.add(link)
            pos = link.endIndex
        }
        if (pos < input.length) {
            result.add(Span(pos, input.length))
        }

        // If no links were found, return a single text span
        if (result.isEmpty()) {
            result.add(Span(0, input.length))
        }

        return result
    }

    /**
     * Finds all matches of [regex] in [input], trims trailing punctuation, and adds valid matches
     * to [results] as [LinkSpan] instances of the given [type].
     */
    private fun findLinks(
        input: String,
        regex: Regex,
        type: LinkType,
        results: MutableList<LinkSpan>
    ) {
        for (match in regex.findAll(input)) {
            val start = match.range.first
            var end = match.range.last + 1
            end = trimTrailingPunctuation(input, start, end)
            if (end > start) {
                // For WWW links, ensure there is at least one dot after "www."
                // (i.e., "www." alone is not a valid link)
                if (type == LinkType.WWW) {
                    val matched = input.substring(start, end)
                    if (!matched.substring(4).contains('.')) {
                        continue
                    }
                }
                results.add(LinkSpan(start, end, type))
            }
        }
    }

    /**
     * Trims trailing punctuation characters from a matched link span. Handles special cases such as
     * balanced parentheses (common in Wikipedia URLs) and HTML entity-like sequences ending with
     * semicolons.
     *
     * @return the adjusted end index after trimming
     */
    private fun trimTrailingPunctuation(input: String, start: Int, end: Int): Int {
        var e = end
        while (e > start) {
            val c = input[e - 1]
            when {
                c in TRAILING_PUNCTUATION -> {
                    e--
                }

                c == ')' -> {
                    // Handle balanced parentheses: only strip closing parens if they are unbalanced
                    val sub = input.substring(start, e)
                    val opens = sub.count { it == '(' }
                    val closes = sub.count { it == ')' }
                    if (closes > opens) {
                        e--
                    } else {
                        break
                    }
                }

                c == ';' -> {
                    // Check if this semicolon is part of an HTML entity like &amp;
                    val sub = input.substring(start, e)
                    val ampIdx = sub.lastIndexOf('&')
                    if (ampIdx >= 0) {
                        val entity = sub.substring(ampIdx)
                        if (entity.matches(Regex("""&[a-zA-Z0-9]+;"""))) {
                            e = start + ampIdx
                        } else {
                            e--
                        }
                    } else {
                        e--
                    }
                }

                else -> break
            }
        }
        return e
    }
}
