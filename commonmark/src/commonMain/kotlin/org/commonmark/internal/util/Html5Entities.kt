package org.commonmark.internal.util

internal object Html5Entities {
    private val NAMED_CHARACTER_REFERENCES: Map<String, String> = Html5EntitiesData.NAMED_CHARACTER_REFERENCES

    fun entityToString(input: String): String {
        if (!input.startsWith("&") || !input.endsWith(";")) {
            return input
        }

        var value = input.substring(1, input.length - 1)
        if (value.startsWith("#")) {
            value = value.substring(1)
            var base = 10
            if (value.startsWith("x") || value.startsWith("X")) {
                value = value.substring(1)
                base = 16
            }

            return try {
                val codePoint = value.toInt(base)
                if (codePoint == 0) {
                    "\uFFFD"
                } else {
                    codePointToString(codePoint)
                }
            } catch (e: NumberFormatException) {
                "\uFFFD"
            } catch (e: IllegalArgumentException) {
                "\uFFFD"
            }
        } else {
            return NAMED_CHARACTER_REFERENCES[value] ?: input
        }
    }

    private fun codePointToString(codePoint: Int): String =
        if (codePoint in 0..0xFFFF) {
            codePoint.toChar().toString()
        } else if (codePoint in 0x10000..0x10FFFF) {
            // Encode as a surrogate pair
            val high = ((codePoint - 0x10000) shr 10) + 0xD800
            val low = ((codePoint - 0x10000) and 0x3FF) + 0xDC00
            charArrayOf(high.toChar(), low.toChar()).concatToString()
        } else {
            "\uFFFD"
        }
}
