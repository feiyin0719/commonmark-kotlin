package org.commonmark.ext.heading.anchor

import org.commonmark.Extension
import org.commonmark.ext.heading.anchor.internal.HeadingIdAttributeProvider
import org.commonmark.renderer.html.HtmlRenderer

/**
 * Extension for adding auto generated IDs to headings.
 *
 * Create it with [create] or [builder] and then configure it on the
 * renderer builder ([HtmlRenderer.Builder.extensions]).
 *
 * The heading text will be used to create the id. Multiple headings with the
 * same text will result in appending a hyphen and number. For example:
 * ```
 * # Heading
 * # Heading
 * ```
 * will result in
 * ```
 * <h1 id="heading">Heading</h1>
 * <h1 id="heading-1">Heading</h1>
 * ```
 *
 * @see IdGenerator the IdGenerator class if just the ID generation part is needed
 */
public class HeadingAnchorExtension private constructor(builder: Builder) :
    HtmlRenderer.HtmlRendererExtension {

    private val defaultId: String = builder.defaultId
    private val idPrefix: String = builder.idPrefix
    private val idSuffix: String = builder.idSuffix

    override fun extend(rendererBuilder: HtmlRenderer.Builder) {
        rendererBuilder.attributeProviderFactory { _ ->
            HeadingIdAttributeProvider.create(defaultId, idPrefix, idSuffix)
        }
    }

    /**
     * Builder for configuring [HeadingAnchorExtension].
     */
    public class Builder {
        internal var defaultId: String = "id"
        internal var idPrefix: String = ""
        internal var idSuffix: String = ""

        /**
         * @param value Default value for the id to take if no generated id can be extracted. Default "id"
         * @return `this`
         */
        public fun defaultId(value: String): Builder {
            this.defaultId = value
            return this
        }

        /**
         * @param value Set the value to be prepended to every id generated. Default ""
         * @return `this`
         */
        public fun idPrefix(value: String): Builder {
            this.idPrefix = value
            return this
        }

        /**
         * @param value Set the value to be appended to every id generated. Default ""
         * @return `this`
         */
        public fun idSuffix(value: String): Builder {
            this.idSuffix = value
            return this
        }

        /**
         * @return a configured extension
         */
        public fun build(): Extension {
            return HeadingAnchorExtension(this)
        }
    }

    public companion object {
        /**
         * @return the extension built with default settings
         */
        public fun create(): Extension = HeadingAnchorExtension(Builder())

        /**
         * @return a builder to configure the extension settings
         */
        public fun builder(): Builder = Builder()
    }
}
