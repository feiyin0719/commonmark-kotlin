package org.commonmark.parser

import org.commonmark.Extension
import org.commonmark.internal.Definitions
import org.commonmark.internal.DocumentParser
import org.commonmark.internal.InlineParserContextImpl
import org.commonmark.internal.InlineParserImpl
import org.commonmark.node.Block
import org.commonmark.node.Node
import org.commonmark.parser.beta.InlineContentParserFactory
import org.commonmark.parser.beta.LinkProcessor
import org.commonmark.parser.block.BlockParserFactory
import org.commonmark.parser.delimiter.DelimiterProcessor
import kotlin.reflect.KClass

/**
 * Parses input text to a tree of nodes.
 *
 * Start with the [builder] method, configure the parser and build it. Example:
 * ```
 * val parser = Parser.builder().build()
 * val document = parser.parse("input text")
 * ```
 */
public class Parser private constructor(
    builder: Builder,
) {
    private val blockParserFactories: List<BlockParserFactory>
    private val inlineContentParserFactories: List<InlineContentParserFactory>
    private val delimiterProcessors: List<DelimiterProcessor>
    private val linkProcessors: List<LinkProcessor>
    private val linkMarkers: Set<Char>
    private val inlineParserFactory: InlineParserFactory
    private val postProcessors: List<PostProcessor>
    private val includeSourceSpans: IncludeSourceSpans

    init {
        this.blockParserFactories =
            DocumentParser.calculateBlockParserFactories(
                builder.blockParserFactories,
                builder.enabledBlockTypes,
            )
        this.inlineParserFactory = builder.getInlineParserFactory()
        this.postProcessors = builder.postProcessors.toList()
        this.inlineContentParserFactories = builder.inlineContentParserFactories.toList()
        this.delimiterProcessors = builder.delimiterProcessors.toList()
        this.linkProcessors = builder.linkProcessors.toList()
        this.linkMarkers = builder.linkMarkers.toSet()
        this.includeSourceSpans = builder.includeSourceSpans

        // Try to construct an inline parser. Invalid configuration might result in an exception, which we want to
        // detect as soon as possible.
        val context =
            InlineParserContextImpl(
                inlineContentParserFactories,
                delimiterProcessors,
                linkProcessors,
                linkMarkers,
                Definitions(),
            )
        this.inlineParserFactory.create(context)
    }

    /**
     * Parse the specified input text into a tree of nodes.
     *
     * This method is thread-safe (a new parser state is used for each invocation).
     *
     * @param input the text to parse - must not be null
     * @return the root node
     */
    public fun parse(input: String): Node {
        val documentParser = createDocumentParser()
        val document = documentParser.parse(input)
        return postProcess(document)
    }

    private fun createDocumentParser(): DocumentParser =
        DocumentParser(
            blockParserFactories,
            inlineParserFactory,
            inlineContentParserFactories,
            delimiterProcessors,
            linkProcessors,
            linkMarkers,
            includeSourceSpans,
        )

    private fun postProcess(document: Node): Node {
        var node = document
        for (postProcessor in postProcessors) {
            node = postProcessor.process(node)
        }
        return node
    }

    public companion object {
        /**
         * Create a new builder for configuring a [Parser].
         *
         * @return a builder
         */
        public fun builder(): Builder = Builder()
    }

    /**
     * Builder for configuring a [Parser].
     */
    public class Builder {
        internal val blockParserFactories = mutableListOf<BlockParserFactory>()
        internal val inlineContentParserFactories = mutableListOf<InlineContentParserFactory>()
        internal val delimiterProcessors = mutableListOf<DelimiterProcessor>()
        internal val linkProcessors = mutableListOf<LinkProcessor>()
        internal val postProcessors = mutableListOf<PostProcessor>()
        internal val linkMarkers = mutableSetOf<Char>()
        internal var enabledBlockTypes: Set<KClass<out Block>> = DocumentParser.getDefaultBlockParserTypes()
        private var inlineParserFactory: InlineParserFactory? = null
        internal var includeSourceSpans: IncludeSourceSpans = IncludeSourceSpans.NONE

        /**
         * @return the configured [Parser]
         */
        public fun build(): Parser = Parser(this)

        /**
         * @param extensions extensions to use on this parser
         * @return `this`
         */
        public fun extensions(extensions: Iterable<Extension>): Builder {
            for (extension in extensions) {
                if (extension is ParserExtension) {
                    extension.extend(this)
                }
            }
            return this
        }

        /**
         * Describe the list of markdown features the parser will recognize and parse.
         *
         * By default, CommonMark will recognize and parse the following set of "block" elements:
         * - [org.commonmark.node.Heading] (`#`)
         * - [org.commonmark.node.HtmlBlock] (`<html></html>`)
         * - [org.commonmark.node.ThematicBreak] (Horizontal Rule) (`---`)
         * - [org.commonmark.node.FencedCodeBlock] (`` ``` ``)
         * - [org.commonmark.node.IndentedCodeBlock]
         * - [org.commonmark.node.BlockQuote] (`>`)
         * - [org.commonmark.node.ListBlock] (Ordered / Unordered List) (`1. / *`)
         *
         * To parse only a subset of the features listed above, pass a set of each feature's associated [Block] class.
         *
         * E.g., to only parse headings and lists:
         * ```
         * Parser.builder().enabledBlockTypes(setOf(Heading::class, ListBlock::class))
         * ```
         *
         * @param enabledBlockTypes A set of block nodes the parser will parse.
         *                          If this set is empty, the parser will not recognize any CommonMark core features.
         * @return `this`
         */
        public fun enabledBlockTypes(enabledBlockTypes: Set<KClass<out Block>>): Builder {
            DocumentParser.checkEnabledBlockTypes(enabledBlockTypes)
            this.enabledBlockTypes = enabledBlockTypes
            return this
        }

        /**
         * Whether to calculate source positions for parsed [Node]s, see [Node.getSourceSpans].
         *
         * By default, source spans are disabled.
         *
         * @param includeSourceSpans which kind of source spans should be included
         * @return `this`
         */
        public fun includeSourceSpans(includeSourceSpans: IncludeSourceSpans): Builder {
            this.includeSourceSpans = includeSourceSpans
            return this
        }

        /**
         * Add a custom block parser factory.
         *
         * Note that custom factories are applied *before* the built-in factories. This is so that
         * extensions can change how some syntax is parsed that would otherwise be handled by built-in factories.
         * "With great power comes great responsibility."
         *
         * @param blockParserFactory a block parser factory implementation
         * @return `this`
         */
        public fun customBlockParserFactory(blockParserFactory: BlockParserFactory): Builder {
            blockParserFactories.add(blockParserFactory)
            return this
        }

        /**
         * Add a factory for a custom inline content parser, for extending inline parsing or overriding built-in parsing.
         *
         * Note that parsers are triggered based on a special character as specified by
         * [InlineContentParserFactory.triggerCharacters]. It is possible to register multiple parsers for the same
         * character, or even for some built-in special character such as `` ` ``. The custom parsers are tried first
         * in order in which they are registered, and then the built-in ones.
         */
        public fun customInlineContentParserFactory(inlineContentParserFactory: InlineContentParserFactory): Builder {
            inlineContentParserFactories.add(inlineContentParserFactory)
            return this
        }

        /**
         * Add a custom delimiter processor for inline parsing.
         *
         * Note that multiple delimiter processors with the same characters can be added, as long as they have a
         * different minimum length. In that case, the processor with the shortest matching length is used. Adding more
         * than one delimiter processor with the same character and minimum length is invalid.
         *
         * If you want more control over how parsing is done, you might want to use
         * [customInlineContentParserFactory] instead.
         *
         * @param delimiterProcessor a delimiter processor implementation
         * @return `this`
         */
        public fun customDelimiterProcessor(delimiterProcessor: DelimiterProcessor): Builder {
            delimiterProcessors.add(delimiterProcessor)
            return this
        }

        /**
         * Add a custom link/image processor for inline parsing.
         *
         * Multiple link processors can be added, and will be tried in order in which they were added. If no link
         * processor applies, the normal behavior applies. That means these can override built-in link parsing.
         *
         * @param linkProcessor a link processor implementation
         * @return `this`
         */
        public fun linkProcessor(linkProcessor: LinkProcessor): Builder {
            linkProcessors.add(linkProcessor)
            return this
        }

        /**
         * Add a custom link marker for link processing. A link marker is a character like `!` which, if it
         * appears before the `[` of a link, changes the meaning of the link.
         *
         * If a link marker followed by a valid link is parsed, the [org.commonmark.parser.beta.LinkInfo]
         * that is passed to [LinkProcessor] will have its [org.commonmark.parser.beta.LinkInfo.marker] set.
         * A link processor should check the [org.commonmark.node.Text.literal] and then do any processing,
         * and will probably want to use [org.commonmark.parser.beta.LinkResult.includeMarker].
         *
         * @param linkMarker a link marker character
         * @return `this`
         */
        public fun linkMarker(linkMarker: Char): Builder {
            linkMarkers.add(linkMarker)
            return this
        }

        public fun postProcessor(postProcessor: PostProcessor): Builder {
            postProcessors.add(postProcessor)
            return this
        }

        /**
         * Overrides the parser used for inline markdown processing.
         *
         * Provide an implementation of InlineParserFactory which provides a custom inline parser
         * to modify how the following are parsed:
         * bold (**)
         * italic (*)
         * strikethrough (~~)
         * backtick quote (`)
         * link ([title](http://))
         * image (![alt](http://))
         *
         * Note that if this method is not called or the inline parser factory is set to null, then the default
         * implementation will be used.
         *
         * @param inlineParserFactory an inline parser factory implementation
         * @return `this`
         */
        public fun inlineParserFactory(inlineParserFactory: InlineParserFactory?): Builder {
            this.inlineParserFactory = inlineParserFactory
            return this
        }

        internal fun getInlineParserFactory(): InlineParserFactory = inlineParserFactory ?: InlineParserFactory { context -> InlineParserImpl(context) }
    }

    /**
     * Extension for [Parser].
     */
    public interface ParserExtension : Extension {
        public fun extend(parserBuilder: Builder)
    }
}
