package org.commonmark.text

/**
 * Functions for finding characters in strings or checking characters.
 */
public object Characters {
    public fun find(
        c: Char,
        s: CharSequence,
        startIndex: Int,
    ): Int {
        val length = s.length
        for (i in startIndex until length) {
            if (s[i] == c) {
                return i
            }
        }
        return -1
    }

    public fun findLineBreak(
        s: CharSequence,
        startIndex: Int,
    ): Int {
        val length = s.length
        for (i in startIndex until length) {
            when (s[i]) {
                '\n', '\r' -> return i
            }
        }
        return -1
    }

    public fun isBlank(s: CharSequence): Boolean = skipSpaceTab(s, 0, s.length) == s.length

    public fun hasNonSpace(s: CharSequence): Boolean {
        val length = s.length
        val skipped = skip(' ', s, 0, length)
        return skipped != length
    }

    public fun isLetter(
        s: CharSequence,
        index: Int,
    ): Boolean {
        val codePoint = s.codePointAt(index)
        return codePoint.isLetterCodePoint()
    }

    public fun isSpaceOrTab(
        s: CharSequence,
        index: Int,
    ): Boolean {
        if (index < s.length) {
            when (s[index]) {
                ' ', '\t' -> return true
            }
        }
        return false
    }

    public fun isPunctuationCodePoint(codePoint: Int): Boolean {
        val category = codePoint.charCategory()
        return when (category) {
            CharCategory.DASH_PUNCTUATION,
            CharCategory.START_PUNCTUATION,
            CharCategory.END_PUNCTUATION,
            CharCategory.CONNECTOR_PUNCTUATION,
            CharCategory.OTHER_PUNCTUATION,
            CharCategory.INITIAL_QUOTE_PUNCTUATION,
            CharCategory.FINAL_QUOTE_PUNCTUATION,
            CharCategory.MATH_SYMBOL,
            CharCategory.CURRENCY_SYMBOL,
            CharCategory.MODIFIER_SYMBOL,
            CharCategory.OTHER_SYMBOL,
            -> {
                true
            }

            else -> {
                when (codePoint) {
                    '$'.code, '+'.code, '<'.code, '='.code, '>'.code,
                    '^'.code, '`'.code, '|'.code, '~'.code,
                    -> true

                    else -> false
                }
            }
        }
    }

    public fun isWhitespaceCodePoint(codePoint: Int): Boolean =
        when (codePoint) {
            ' '.code, '\t'.code, '\n'.code, '\u000C'.code, '\r'.code -> true
            else -> codePoint.charCategory() == CharCategory.SPACE_SEPARATOR
        }

    public fun skip(
        skip: Char,
        s: CharSequence,
        startIndex: Int,
        endIndex: Int,
    ): Int {
        for (i in startIndex until endIndex) {
            if (s[i] != skip) {
                return i
            }
        }
        return endIndex
    }

    public fun skipBackwards(
        skip: Char,
        s: CharSequence,
        startIndex: Int,
        lastIndex: Int,
    ): Int {
        for (i in startIndex downTo lastIndex) {
            if (s[i] != skip) {
                return i
            }
        }
        return lastIndex - 1
    }

    public fun skipSpaceTab(
        s: CharSequence,
        startIndex: Int,
        endIndex: Int,
    ): Int {
        for (i in startIndex until endIndex) {
            when (s[i]) {
                ' ', '\t' -> continue
                else -> return i
            }
        }
        return endIndex
    }

    public fun skipSpaceTabBackwards(
        s: CharSequence,
        startIndex: Int,
        lastIndex: Int,
    ): Int {
        for (i in startIndex downTo lastIndex) {
            when (s[i]) {
                ' ', '\t' -> continue
                else -> return i
            }
        }
        return lastIndex - 1
    }
}

internal fun CharSequence.codePointAt(index: Int): Int {
    val high = this[index]
    if (high.isHighSurrogate() && index + 1 < length) {
        val low = this[index + 1]
        if (low.isLowSurrogate()) {
            return toCodePoint(high, low)
        }
    }
    return high.code
}

internal fun toCodePoint(
    high: Char,
    low: Char,
): Int = ((high.code - 0xD800) shl 10) + (low.code - 0xDC00) + 0x10000

private fun Int.isLetterCodePoint(): Boolean {
    if (this <= 0xFFFF) {
        return this.toChar().isLetter()
    }
    val category = this.charCategory()
    return category == CharCategory.UPPERCASE_LETTER ||
        category == CharCategory.LOWERCASE_LETTER ||
        category == CharCategory.TITLECASE_LETTER ||
        category == CharCategory.MODIFIER_LETTER ||
        category == CharCategory.OTHER_LETTER
}

internal expect fun Int.charCategory(): CharCategory

internal fun Int.toChars(): CharArray {
    if (this < 0x10000) {
        return charArrayOf(this.toChar())
    }
    val offset = this - 0x10000
    return charArrayOf(
        ((offset ushr 10) + 0xD800).toChar(),
        ((offset and 0x3FF) + 0xDC00).toChar(),
    )
}
