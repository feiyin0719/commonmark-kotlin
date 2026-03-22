package org.commonmark.test

import org.commonmark.node.FencedCodeBlock
import org.commonmark.parser.Parser
import kotlin.test.Test
import kotlin.test.assertEquals

class FencedCodeBlockParserTest : CoreRenderingTestCase() {

    @Test
    fun backtickInfo() {
        val parser = Parser.builder().build()
        val document = parser.parse("```info ~ test\ncode\n```")
        val codeBlock = document.firstChild as FencedCodeBlock
        assertEquals("info ~ test", codeBlock.info)
        assertEquals("code\n", codeBlock.literal)
    }

    @Test
    fun backtickInfoDoesntAllowBacktick() {
        assertRendering(
            "```info ` test\ncode\n```",
            "<p>```info ` test\ncode</p>\n<pre><code></code></pre>\n"
        )
    }

    @Test
    fun backtickAndTildeCantBeMixed() {
        assertRendering(
            "``~`\ncode\n``~`",
            "<p><code>~` code </code>~`</p>\n"
        )
    }

    @Test
    fun closingCanHaveSpacesAfter() {
        assertRendering(
            "```\ncode\n```   ",
            "<pre><code>code\n</code></pre>\n"
        )
    }

    @Test
    fun closingCanNotHaveNonSpaces() {
        assertRendering(
            "```\ncode\n``` a",
            "<pre><code>code\n``` a\n</code></pre>\n"
        )
    }

    @Test
    fun issue151() {
        assertRendering(
            "```\nthis code\n\nshould not have BRs or paragraphs in it\nok\n```",
            "<pre><code>this code\n" +
                    "\n" +
                    "should not have BRs or paragraphs in it\n" +
                    "ok\n" +
                    "</code></pre>\n"
        )
    }
}
