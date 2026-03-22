package org.commonmark.ext.autolink

import org.commonmark.Extension
import org.commonmark.ext.autolink.internal.AutolinkPostProcessor
import org.commonmark.parser.Parser

/**
 * Extension for automatically turning plain URLs and email addresses into links.
 *
 * Create it with [create] and then configure it on the builders
 * ([Parser.Builder.extensions]).
 *
 * The parsed links are turned into normal [org.commonmark.node.Link] nodes.
 */
public class AutolinkExtension private constructor(
    builder: Builder,
) : Parser.ParserExtension {
    private val linkTypes: Set<AutolinkType> = builder.linkTypes.toSet()

    override fun extend(parserBuilder: Parser.Builder) {
        parserBuilder.postProcessor(AutolinkPostProcessor(linkTypes))
    }

    public companion object {
        /**
         * Create the extension with default options (all link types enabled).
         */
        public fun create(): Extension = builder().build()

        /**
         * Create a builder to configure the behavior of the extension.
         */
        public fun builder(): Builder = Builder()
    }

    public class Builder {
        internal var linkTypes: Set<AutolinkType> = AutolinkType.entries.toSet()

        /**
         * Configure the link types that should be autolinked. By default, all [AutolinkType] values
         * are enabled.
         *
         * @param linkTypes the link types to enable
         * @return this builder
         */
        public fun linkTypes(vararg linkTypes: AutolinkType): Builder = this.linkTypes(linkTypes.toSet())

        /**
         * Configure the link types that should be autolinked. By default, all [AutolinkType] values
         * are enabled.
         *
         * @param linkTypes the link types to enable
         * @return this builder
         */
        public fun linkTypes(linkTypes: Set<AutolinkType>): Builder {
            require(linkTypes.isNotEmpty()) { "linkTypes must not be empty" }
            this.linkTypes = linkTypes.toSet()
            return this
        }

        /**
         * Build the configured extension.
         */
        public fun build(): Extension = AutolinkExtension(this)
    }
}
