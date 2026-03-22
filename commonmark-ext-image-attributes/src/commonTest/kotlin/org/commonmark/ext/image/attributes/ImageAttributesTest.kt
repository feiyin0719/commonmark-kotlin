package org.commonmark.ext.image.attributes

import org.commonmark.node.Paragraph
import org.commonmark.node.SourceSpan
import org.commonmark.parser.IncludeSourceSpans
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import kotlin.test.Test
import kotlin.test.assertEquals

class ImageAttributesTest {
    private val extensions = setOf(ImageAttributesExtension.create())
    private val parser = Parser.builder().extensions(extensions).build()
    private val renderer = HtmlRenderer.builder().extensions(extensions).build()

    private fun render(source: String): String = renderer.render(parser.parse(source))

    private fun assertRendering(
        source: String,
        expected: String,
    ) {
        assertEquals(expected, render(source))
    }

    @Test
    fun baseCase() {
        assertRendering(
            "![text](/url.png){height=5}",
            "<p><img src=\"/url.png\" alt=\"text\" height=\"5\" /></p>\n",
        )

        assertRendering(
            "![text](/url.png){height=5 width=6}",
            "<p><img src=\"/url.png\" alt=\"text\" height=\"5\" width=\"6\" /></p>\n",
        )

        assertRendering(
            "![text](/url.png){height=99px   width=100px}",
            "<p><img src=\"/url.png\" alt=\"text\" height=\"99px\" width=\"100px\" /></p>\n",
        )

        assertRendering(
            "![text](/url.png){width=100 height=100}",
            "<p><img src=\"/url.png\" alt=\"text\" width=\"100\" height=\"100\" /></p>\n",
        )

        assertRendering(
            "![text](/url.png){height=4.8 width=3.14}",
            "<p><img src=\"/url.png\" alt=\"text\" height=\"4.8\" width=\"3.14\" /></p>\n",
        )

        assertRendering(
            "![text](/url.png){Width=18 HeIgHt=1001}",
            "<p><img src=\"/url.png\" alt=\"text\" Width=\"18\" HeIgHt=\"1001\" /></p>\n",
        )

        assertRendering(
            "![text](/url.png){height=green width=blue}",
            "<p><img src=\"/url.png\" alt=\"text\" height=\"green\" width=\"blue\" /></p>\n",
        )
    }

    @Test
    fun doubleDelimiters() {
        assertRendering(
            "![text](/url.png){{height=5}}",
            "<p><img src=\"/url.png\" alt=\"text\" />{{height=5}}</p>\n",
        )
    }

    @Test
    fun mismatchingDelimitersAreIgnored() {
        assertRendering("![text](/url.png){", "<p><img src=\"/url.png\" alt=\"text\" />{</p>\n")
    }

    @Test
    fun unsupportedStyleNamesAreLeftUnchanged() {
        assertRendering(
            "![text](/url.png){j=502 K=101 img=2 url=5}",
            "<p><img src=\"/url.png\" alt=\"text\" />{j=502 K=101 img=2 url=5}</p>\n",
        )
        assertRendering(
            "![foo](/url.png){height=3 invalid}\n",
            "<p><img src=\"/url.png\" alt=\"foo\" />{height=3 invalid}</p>\n",
        )
        assertRendering(
            "![foo](/url.png){height=3 *test*}\n",
            "<p><img src=\"/url.png\" alt=\"foo\" />{height=3 <em>test</em>}</p>\n",
        )
    }

    @Test
    fun styleWithNoValueIsIgnored() {
        assertRendering(
            "![text](/url.png){height}",
            "<p><img src=\"/url.png\" alt=\"text\" />{height}</p>\n",
        )
    }

    @Test
    fun repeatedStyleNameUsesFinalOne() {
        assertRendering(
            "![text](/url.png){height=4 height=5 width=1 height=6}",
            "<p><img src=\"/url.png\" alt=\"text\" height=\"6\" width=\"1\" /></p>\n",
        )
    }

    @Test
    fun styleValuesAreEscaped() {
        assertRendering(
            "![text](/url.png){height=<img}",
            "<p><img src=\"/url.png\" alt=\"text\" height=\"&lt;img\" /></p>\n",
        )
        assertRendering(
            "![text](/url.png){height=\"\"img}",
            "<p><img src=\"/url.png\" alt=\"text\" height=\"&quot;&quot;img\" /></p>\n",
        )
    }

    @Test
    fun imageAltTextWithSpaces() {
        assertRendering(
            "![Android SDK Manager](/contrib/android-sdk-manager.png){height=502 width=101}",
            "<p><img src=\"/contrib/android-sdk-manager.png\" alt=\"Android SDK Manager\" height=\"502\" width=\"101\" /></p>\n",
        )
    }

    @Test
    fun imageAltTextWithSoftLineBreak() {
        assertRendering(
            "![foo\nbar](/url){height=101 width=202}\n",
            "<p><img src=\"/url\" alt=\"foo\nbar\" height=\"101\" width=\"202\" /></p>\n",
        )
    }

    @Test
    fun imageAltTextWithHardLineBreak() {
        assertRendering(
            "![foo  \nbar](/url){height=506 width=1}\n",
            "<p><img src=\"/url\" alt=\"foo\nbar\" height=\"506\" width=\"1\" /></p>\n",
        )
    }

    @Test
    fun imageAltTextWithEntities() {
        assertRendering(
            "![foo &auml;](/url){height=99 width=100}\n",
            "<p><img src=\"/url\" alt=\"foo \u00E4\" height=\"99\" width=\"100\" /></p>\n",
        )
    }

    @Test
    fun textNodesAreUnchanged() {
        assertRendering("x{height=3 width=4}\n", "<p>x{height=3 width=4}</p>\n")
        assertRendering("x {height=3 width=4}\n", "<p>x {height=3 width=4}</p>\n")
        assertRendering("\\documentclass[12pt]{article}\n", "<p>\\documentclass[12pt]{article}</p>\n")
        assertRendering("some *text*{height=3 width=4}\n", "<p>some <em>text</em>{height=3 width=4}</p>\n")
        assertRendering("{NN} text", "<p>{NN} text</p>\n")
        assertRendering("{}", "<p>{}</p>\n")
    }

    @Test
    fun sourceSpans() {
        val parser =
            Parser
                .builder()
                .extensions(extensions)
                .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
                .build()

        // This doesn't result in image attributes, so source spans should be for the single (merged) text node.
        val document = parser.parse("x{height=3 width=4}\n")
        val block = document.firstChild as Paragraph
        val text = block.firstChild!!
        assertEquals(listOf(SourceSpan.of(0, 0, 0, 19)), text.getSourceSpans())
    }
}
