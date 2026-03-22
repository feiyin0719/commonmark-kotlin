package org.commonmark.text

/**
 * Char matcher that can match ASCII characters efficiently.
 */
public class AsciiMatcher private constructor(
    builder: Builder,
) : CharMatcher {
    private val lo: Long = builder.lo
    private val hi: Long = builder.hi

    override fun matches(c: Char): Boolean {
        val code = c.code
        if (code > 127) return false
        return if (code < 64) {
            (lo and (1L shl code)) != 0L
        } else {
            (hi and (1L shl (code - 64))) != 0L
        }
    }

    public fun newBuilder(): Builder = Builder(lo, hi)

    public companion object {
        public fun builder(): Builder = Builder(0L, 0L)

        public fun builder(matcher: AsciiMatcher): Builder = Builder(matcher.lo, matcher.hi)
    }

    public class Builder internal constructor(
        internal var lo: Long,
        internal var hi: Long,
    ) {
        public fun c(c: Char): Builder {
            val code = c.code
            require(code <= 127) { "Can only match ASCII characters" }
            if (code < 64) {
                lo = lo or (1L shl code)
            } else {
                hi = hi or (1L shl (code - 64))
            }
            return this
        }

        public fun anyOf(s: String): Builder {
            for (ch in s) {
                c(ch)
            }
            return this
        }

        public fun anyOf(characters: Set<Char>): Builder {
            for (ch in characters) {
                c(ch)
            }
            return this
        }

        public fun range(
            from: Char,
            toInclusive: Char,
        ): Builder {
            var c = from
            while (c <= toInclusive) {
                c(c)
                c++
            }
            return this
        }

        public fun build(): AsciiMatcher = AsciiMatcher(this)
    }
}
