package org.commonmark.ext.heading.anchor

import org.commonmark.Extension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import kotlin.test.Test
import kotlin.test.assertEquals

class HeadingAnchorConfigurationTest {

    private val parser: Parser = Parser.builder().build()

    private fun buildRenderer(defaultId: String, prefix: String, suffix: String): HtmlRenderer {
        val ext: Extension = HeadingAnchorExtension.builder()
            .defaultId(defaultId)
            .idPrefix(prefix)
            .idSuffix(suffix)
            .build()
        return HtmlRenderer.builder()
            .extensions(listOf(ext))
            .build()
    }

    private fun doRender(renderer: HtmlRenderer, text: String): String {
        return renderer.render(parser.parse(text))
    }

    @Test
    fun testDefaultConfigurationHasNoAdditions() {
        val renderer = HtmlRenderer.builder()
            .extensions(listOf(HeadingAnchorExtension.create()))
            .build()
        assertEquals("<h1 id=\"id\"></h1>\n", doRender(renderer, "# "))
    }

    @Test
    fun testDefaultIdWhenNoTextOnHeader() {
        val renderer = buildRenderer("defid", "", "")
        assertEquals("<h1 id=\"defid\"></h1>\n", doRender(renderer, "# "))
    }

    @Test
    fun testPrefixAddedToHeader() {
        val renderer = buildRenderer("", "pre-", "")
        assertEquals("<h1 id=\"pre-text\">text</h1>\n", doRender(renderer, "# text"))
    }

    @Test
    fun testSuffixAddedToHeader() {
        val renderer = buildRenderer("", "", "-post")
        assertEquals("<h1 id=\"text-post\">text</h1>\n", doRender(renderer, "# text"))
    }
}
