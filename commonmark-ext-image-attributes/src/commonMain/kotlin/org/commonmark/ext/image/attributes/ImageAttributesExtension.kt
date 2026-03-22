package org.commonmark.ext.image.attributes

import org.commonmark.Extension
import org.commonmark.ext.image.attributes.internal.ImageAttributesAttributeProvider
import org.commonmark.ext.image.attributes.internal.ImageAttributesDelimiterProcessor
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

/**
 * Extension for adding attributes to image nodes.
 *
 * Create it with [create] and then configure it on the builders
 * ([Parser.Builder.extensions], [HtmlRenderer.Builder.extensions]).
 */
public class ImageAttributesExtension private constructor() :
    Parser.ParserExtension,
    HtmlRenderer.HtmlRendererExtension {
        public companion object {
            public fun create(): Extension = ImageAttributesExtension()
        }

        override fun extend(parserBuilder: Parser.Builder) {
            parserBuilder.customDelimiterProcessor(ImageAttributesDelimiterProcessor())
        }

        override fun extend(rendererBuilder: HtmlRenderer.Builder) {
            rendererBuilder.attributeProviderFactory { ImageAttributesAttributeProvider.create() }
        }
    }
