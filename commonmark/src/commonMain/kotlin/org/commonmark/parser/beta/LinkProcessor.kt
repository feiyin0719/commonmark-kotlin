package org.commonmark.parser.beta

import org.commonmark.parser.InlineParserContext

/**
 * An interface to decide how links/images are handled.
 */
public interface LinkProcessor {
    public fun process(
        linkInfo: LinkInfo,
        scanner: Scanner,
        context: InlineParserContext,
    ): LinkResult?
}
