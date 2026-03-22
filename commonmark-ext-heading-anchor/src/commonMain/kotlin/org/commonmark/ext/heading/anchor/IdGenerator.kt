package org.commonmark.ext.heading.anchor

/**
 * Generates strings to be used as identifiers.
 *
 * Use [builder] to create an instance.
 */
public class IdGenerator private constructor(
    builder: Builder,
) {
    // Use Unicode character classes to match word characters across all platforms.
    // Java uses Pattern.UNICODE_CHARACTER_CLASS to make \w match Unicode; in Kotlin we use explicit Unicode categories:
    // \p{L} (letters), \p{N} (numbers), \p{Pc} (connector punctuation like _ and ‿), \p{M} (combining marks)
    private val allowedCharacters: Regex = Regex("[\\p{L}\\p{N}\\p{Pc}\\p{M}\\-]+")
    private val identityMap: MutableMap<String, Int> = mutableMapOf()
    private val prefix: String = builder.prefix
    private val suffix: String = builder.suffix
    private var defaultIdentifier: String = builder.defaultIdentifier

    /**
     * Generate an ID based on the provided text and previously generated IDs.
     *
     * This method is not thread safe, concurrent calls can end up
     * with non-unique identifiers.
     *
     * Note that collision can occur in the case that
     * - Method called with 'X'
     * - Method called with 'X' again
     * - Method called with 'X-1'
     *
     * In that case, the three generated IDs will be:
     * - X
     * - X-1
     * - X-1
     *
     * Therefore if collisions are unacceptable you should ensure that
     * numbers are stripped from end of [text].
     *
     * @param text Text that the identifier should be based on. Will be normalised, then used to generate the
     * identifier.
     * @return [text] if this is the first instance that the [text] has been passed
     * to the method. Otherwise, `text + "-" + X` will be returned, where X is the number of times
     * that [text] has previously been passed in. If [text] is empty, the default
     * identifier given in the constructor will be used.
     */
    public fun generateId(text: String?): String {
        var normalizedIdentity = if (text != null) normalizeText(text) else defaultIdentifier

        if (normalizedIdentity.isEmpty()) {
            normalizedIdentity = defaultIdentifier
        }

        val currentCount = identityMap[normalizedIdentity]
        return if (currentCount == null) {
            identityMap[normalizedIdentity] = 1
            prefix + normalizedIdentity + suffix
        } else {
            identityMap[normalizedIdentity] = currentCount + 1
            prefix + normalizedIdentity + "-" + currentCount + suffix
        }
    }

    /**
     * Assume we've been given a space separated text.
     *
     * @param text Text to normalize to an ID
     */
    private fun normalizeText(text: String): String {
        val firstPassNormalising = text.lowercase().replace(" ", "-")

        val sb = StringBuilder()
        for (match in allowedCharacters.findAll(firstPassNormalising)) {
            sb.append(match.value)
        }

        return sb.toString()
    }

    public class Builder {
        internal var defaultIdentifier: String = "id"
        internal var prefix: String = ""
        internal var suffix: String = ""

        public fun build(): IdGenerator = IdGenerator(this)

        /**
         * @param defaultId the default identifier to use in case the provided text is empty or only contains unusable characters
         * @return `this`
         */
        public fun defaultId(defaultId: String): Builder {
            this.defaultIdentifier = defaultId
            return this
        }

        /**
         * @param prefix the text to place before the generated identity
         * @return `this`
         */
        public fun prefix(prefix: String): Builder {
            this.prefix = prefix
            return this
        }

        /**
         * @param suffix the text to place after the generated identity
         * @return `this`
         */
        public fun suffix(suffix: String): Builder {
            this.suffix = suffix
            return this
        }
    }

    public companion object {
        /**
         * @return a new builder with default arguments
         */
        public fun builder(): Builder = Builder()
    }
}
