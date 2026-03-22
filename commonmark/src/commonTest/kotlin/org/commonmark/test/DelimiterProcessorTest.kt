package org.commonmark.test

import org.commonmark.node.CustomNode
import org.commonmark.node.Node
import org.commonmark.node.Text
import org.commonmark.parser.Parser
import org.commonmark.parser.delimiter.DelimiterProcessor
import org.commonmark.parser.delimiter.DelimiterRun
import org.commonmark.renderer.NodeRenderer
import org.commonmark.renderer.html.HtmlNodeRendererContext
import org.commonmark.renderer.html.HtmlNodeRendererFactory
import org.commonmark.renderer.html.HtmlRenderer
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DelimiterProcessorTest : CoreRenderingTestCase() {
    @Test
    fun delimiterProcessorWithInvalidDelimiterUse() {
        val parser =
            Parser
                .builder()
                .customDelimiterProcessor(CustomDelimiterProcessor(':', 0))
                .customDelimiterProcessor(CustomDelimiterProcessor(';', -1))
                .build()
        assertEquals("<p>:test:</p>\n", RENDERER.render(parser.parse(":test:")))
        assertEquals("<p>;test;</p>\n", RENDERER.render(parser.parse(";test;")))
    }

    @Test
    fun asymmetricDelimiter() {
        assertRendering("{foo} bar", "<p>FOO bar</p>\n")
        assertRendering("f{oo ba}r", "<p>fOO BAr</p>\n")
        assertRendering("{{foo} bar", "<p>{FOO bar</p>\n")
        assertRendering("{foo}} bar", "<p>FOO} bar</p>\n")
        assertRendering("{{foo} bar}", "<p>FOO BAR</p>\n")
        assertRendering("{foo bar", "<p>{foo bar</p>\n")
        assertRendering("foo} bar", "<p>foo} bar</p>\n")
        assertRendering("}foo} bar", "<p>}foo} bar</p>\n")
        assertRendering("{foo{ bar", "<p>{foo{ bar</p>\n")
        assertRendering("}foo{ bar", "<p>}foo{ bar</p>\n")
        assertRendering("{} {foo}", "<p> FOO</p>\n")
    }

    @Test
    fun multipleDelimitersWithDifferentLengths() {
        val parser =
            Parser
                .builder()
                .customDelimiterProcessor(OneDelimiterProcessor())
                .customDelimiterProcessor(TwoDelimiterProcessor())
                .build()
        assertEquals("<p>(1)one(/1) (2)two(/2)</p>\n", RENDERER.render(parser.parse("+one+ ++two++")))
        assertEquals("<p>(1)(2)both(/2)(/1)</p>\n", RENDERER.render(parser.parse("+++both+++")))
    }

    @Test
    fun multipleDelimitersWithSameLengthConflict() {
        assertFailsWith<IllegalArgumentException> {
            Parser
                .builder()
                .customDelimiterProcessor(OneDelimiterProcessor())
                .customDelimiterProcessor(OneDelimiterProcessor())
                .build()
        }
    }

    override fun render(source: String): String {
        val node = PARSER.parse(source)
        return RENDERER.render(node)
    }

    private class CustomDelimiterProcessor(
        private val delimiterChar: Char,
        private val delimiterUse: Int,
    ) : DelimiterProcessor {
        override val openingCharacter: Char get() = delimiterChar
        override val closingCharacter: Char get() = delimiterChar
        override val minLength: Int get() = 1

        override fun process(
            openingRun: DelimiterRun,
            closingRun: DelimiterRun,
        ): Int = delimiterUse
    }

    private class AsymmetricDelimiterProcessor : DelimiterProcessor {
        override val openingCharacter: Char get() = '{'
        override val closingCharacter: Char get() = '}'
        override val minLength: Int get() = 1

        override fun process(
            openingRun: DelimiterRun,
            closingRun: DelimiterRun,
        ): Int {
            val content = UpperCaseNode()
            val start = openingRun.opener
            val end = closingRun.closer
            var tmp: Node? = start.next
            while (tmp != null && tmp != end) {
                val next = tmp.next
                content.appendChild(tmp)
                tmp = next
            }
            start.insertAfter(content)
            return 1
        }
    }

    private class UpperCaseNode : CustomNode()

    private class UpperCaseNodeRenderer(
        private val context: HtmlNodeRendererContext,
    ) : NodeRenderer {
        override fun getNodeTypes(): Set<KClass<out Node>> = setOf(UpperCaseNode::class)

        override fun render(node: Node) {
            val upperCaseNode = node as UpperCaseNode
            var child: Node? = upperCaseNode.firstChild
            while (child != null) {
                if (child is Text) {
                    child.literal = child.literal.uppercase()
                }
                context.render(child)
                child = child.next
            }
        }
    }

    private class OneDelimiterProcessor : DelimiterProcessor {
        override val openingCharacter: Char get() = '+'
        override val closingCharacter: Char get() = '+'
        override val minLength: Int get() = 1

        override fun process(
            openingRun: DelimiterRun,
            closingRun: DelimiterRun,
        ): Int {
            openingRun.opener.insertAfter(Text("(1)"))
            closingRun.closer.insertBefore(Text("(/1)"))
            return 1
        }
    }

    private class TwoDelimiterProcessor : DelimiterProcessor {
        override val openingCharacter: Char get() = '+'
        override val closingCharacter: Char get() = '+'
        override val minLength: Int get() = 2

        override fun process(
            openingRun: DelimiterRun,
            closingRun: DelimiterRun,
        ): Int {
            openingRun.opener.insertAfter(Text("(2)"))
            closingRun.closer.insertBefore(Text("(/2)"))
            return 2
        }
    }

    companion object {
        private val PARSER = Parser.builder().customDelimiterProcessor(AsymmetricDelimiterProcessor()).build()
        private val RENDERER =
            HtmlRenderer
                .builder()
                .nodeRendererFactory(HtmlNodeRendererFactory { context -> UpperCaseNodeRenderer(context) })
                .build()
    }
}
