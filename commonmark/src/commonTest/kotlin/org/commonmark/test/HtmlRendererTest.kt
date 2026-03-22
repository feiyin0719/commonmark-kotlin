package org.commonmark.test

import org.commonmark.node.Document
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.Image
import org.commonmark.node.Link
import org.commonmark.node.Node
import org.commonmark.node.Paragraph
import org.commonmark.parser.Parser
import org.commonmark.renderer.NodeRenderer
import org.commonmark.renderer.html.AttributeProvider
import org.commonmark.renderer.html.AttributeProviderContext
import org.commonmark.renderer.html.AttributeProviderFactory
import org.commonmark.renderer.html.DefaultUrlSanitizer
import org.commonmark.renderer.html.HtmlNodeRendererContext
import org.commonmark.renderer.html.HtmlNodeRendererFactory
import org.commonmark.renderer.html.HtmlRenderer
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlRendererTest {
    @Test
    fun htmlAllowingShouldNotEscapeInlineHtml() {
        val rendered = htmlAllowingRenderer().render(parse("paragraph with <span id='foo' class=\"bar\">inline &amp; html</span>"))
        assertEquals("<p>paragraph with <span id='foo' class=\"bar\">inline &amp; html</span></p>\n", rendered)
    }

    @Test
    fun htmlAllowingShouldNotEscapeBlockHtml() {
        val rendered = htmlAllowingRenderer().render(parse("<div id='foo' class=\"bar\">block &amp;</div>"))
        assertEquals("<div id='foo' class=\"bar\">block &amp;</div>\n", rendered)
    }

    @Test
    fun htmlEscapingShouldEscapeInlineHtml() {
        val rendered = htmlEscapingRenderer().render(parse("paragraph with <span id='foo' class=\"bar\">inline &amp; html</span>"))
        // Note that &amp; is not escaped, as it's a normal text node, not part of the inline HTML.
        assertEquals("<p>paragraph with &lt;span id='foo' class=&quot;bar&quot;&gt;inline &amp; html&lt;/span&gt;</p>\n", rendered)
    }

    @Test
    fun htmlEscapingShouldEscapeHtmlBlocks() {
        val rendered = htmlEscapingRenderer().render(parse("<div id='foo' class=\"bar\">block &amp;</div>"))
        assertEquals("<p>&lt;div id='foo' class=&quot;bar&quot;&gt;block &amp;amp;&lt;/div&gt;</p>\n", rendered)
    }

    @Test
    fun textEscaping() {
        val rendered = defaultRenderer().render(parse("escaping: & < > \" '"))
        assertEquals("<p>escaping: &amp; &lt; &gt; &quot; '</p>\n", rendered)
    }

    @Test
    fun characterReferencesWithoutSemicolonsShouldNotBeParsedShouldBeEscaped() {
        val input = "[example](&#x6A&#x61&#x76&#x61&#x73&#x63&#x72&#x69&#x70&#x74&#x3A&#x61&#x6C&#x65&#x72&#x74&#x28&#x27&#x58&#x53&#x53&#x27&#x29)"
        val rendered = defaultRenderer().render(parse(input))
        assertEquals(
            "<p><a href=\"&amp;#x6A&amp;#x61&amp;#x76&amp;#x61&amp;#x73&amp;#x63&amp;#x72&amp;#x69&amp;#x70&amp;#x74&amp;#x3A&amp;#x61&amp;#x6C&amp;#x65&amp;#x72&amp;#x74&amp;#x28&amp;#x27&amp;#x58&amp;#x53&amp;#x53&amp;#x27&amp;#x29\">example</a></p>\n",
            rendered,
        )
    }

    @Test
    fun attributeEscaping() {
        val paragraph = Paragraph()
        val link = Link()
        link.destination = "&colon;"
        paragraph.appendChild(link)
        assertEquals("<p><a href=\"&amp;colon;\"></a></p>\n", defaultRenderer().render(paragraph))
    }

    @Test
    fun rawUrlsShouldNotFilterDangerousProtocols() {
        val paragraph = Paragraph()
        val link = Link()
        link.destination = "javascript:alert(5);"
        paragraph.appendChild(link)
        assertEquals("<p><a href=\"javascript:alert(5);\"></a></p>\n", rawUrlsRenderer().render(paragraph))
    }

    @Test
    fun sanitizedUrlsShouldSetRelNoFollow() {
        var paragraph = Paragraph()
        var link = Link()
        link.destination = "/exampleUrl"
        paragraph.appendChild(link)
        assertEquals("<p><a rel=\"nofollow\" href=\"/exampleUrl\"></a></p>\n", sanitizeUrlsRenderer().render(paragraph))

        paragraph = Paragraph()
        link = Link()
        link.destination = "https://google.com"
        paragraph.appendChild(link)
        assertEquals("<p><a rel=\"nofollow\" href=\"https://google.com\"></a></p>\n", sanitizeUrlsRenderer().render(paragraph))
    }

    @Test
    fun sanitizedUrlsShouldAllowSafeProtocols() {
        var paragraph = Paragraph()
        var link = Link()
        link.destination = "http://google.com"
        paragraph.appendChild(link)
        assertEquals("<p><a rel=\"nofollow\" href=\"http://google.com\"></a></p>\n", sanitizeUrlsRenderer().render(paragraph))

        paragraph = Paragraph()
        link = Link()
        link.destination = "https://google.com"
        paragraph.appendChild(link)
        assertEquals("<p><a rel=\"nofollow\" href=\"https://google.com\"></a></p>\n", sanitizeUrlsRenderer().render(paragraph))

        paragraph = Paragraph()
        link = Link()
        link.destination = "mailto:foo@bar.example.com"
        paragraph.appendChild(link)
        assertEquals("<p><a rel=\"nofollow\" href=\"mailto:foo@bar.example.com\"></a></p>\n", sanitizeUrlsRenderer().render(paragraph))

        val image = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAAFiUAABYlAUlSJPAAAAAQSURBVBhXY/iPBVBf8P9/AG8TY51nJdgkAAAAAElFTkSuQmCC"
        paragraph = Paragraph()
        link = Link()
        link.destination = image
        paragraph.appendChild(link)
        assertEquals("<p><a rel=\"nofollow\" href=\"$image\"></a></p>\n", sanitizeUrlsRenderer().render(paragraph))
    }

    @Test
    fun sanitizedUrlsShouldFilterDangerousProtocols() {
        var paragraph = Paragraph()
        var link = Link()
        link.destination = "javascript:alert(5);"
        paragraph.appendChild(link)
        assertEquals("<p><a rel=\"nofollow\" href=\"\"></a></p>\n", sanitizeUrlsRenderer().render(paragraph))

        paragraph = Paragraph()
        link = Link()
        link.destination = "ftp://google.com"
        paragraph.appendChild(link)
        assertEquals("<p><a rel=\"nofollow\" href=\"\"></a></p>\n", sanitizeUrlsRenderer().render(paragraph))
    }

    @Test
    fun percentEncodeUrlDisabled() {
        assertEquals("<p><a href=\"foo&amp;bar\">a</a></p>\n", defaultRenderer().render(parse("[a](foo&amp;bar)")))
        assertEquals("<p><a href=\"\u00E4\">a</a></p>\n", defaultRenderer().render(parse("[a](\u00E4)")))
        assertEquals("<p><a href=\"foo%20bar\">a</a></p>\n", defaultRenderer().render(parse("[a](foo%20bar)")))
    }

    @Test
    fun percentEncodeUrl() {
        // Entities are escaped anyway
        assertEquals("<p><a href=\"foo&amp;bar\">a</a></p>\n", percentEncodingRenderer().render(parse("[a](foo&amp;bar)")))
        // Existing encoding is preserved
        assertEquals("<p><a href=\"foo%20bar\">a</a></p>\n", percentEncodingRenderer().render(parse("[a](foo%20bar)")))
        assertEquals("<p><a href=\"foo%61\">a</a></p>\n", percentEncodingRenderer().render(parse("[a](foo%61)")))
        // Invalid encoding is escaped
        assertEquals("<p><a href=\"foo%25\">a</a></p>\n", percentEncodingRenderer().render(parse("[a](foo%)")))
        assertEquals("<p><a href=\"foo%25a\">a</a></p>\n", percentEncodingRenderer().render(parse("[a](foo%a)")))
        assertEquals("<p><a href=\"foo%25a_\">a</a></p>\n", percentEncodingRenderer().render(parse("[a](foo%a_)")))
        assertEquals("<p><a href=\"foo%25xx\">a</a></p>\n", percentEncodingRenderer().render(parse("[a](foo%xx)")))
        // Reserved characters are preserved, except for '[' and ']'
        assertEquals(
            "<p><a href=\"!*'();:@&amp;=+\$,/?#%5B%5D\">a</a></p>\n",
            percentEncodingRenderer().render(parse("[a](!*'();:@&=+\$,/?#[])")),
        )
        // Unreserved characters are preserved
        assertEquals(
            "<p><a href=\"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.~\">a</a></p>\n",
            percentEncodingRenderer().render(parse("[a](ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.~)")),
        )
        // Other characters are percent-encoded (LATIN SMALL LETTER A WITH DIAERESIS)
        assertEquals("<p><a href=\"%C3%A4\">a</a></p>\n", percentEncodingRenderer().render(parse("[a](\u00E4)")))
        // Other characters are percent-encoded (MUSICAL SYMBOL G CLEF, surrogate pair in UTF-16)
        assertEquals("<p><a href=\"%F0%9D%84%9E\">a</a></p>\n", percentEncodingRenderer().render(parse("[a](\uD834\uDD1E)")))
    }

    @Test
    fun attributeProviderForCodeBlock() {
        val custom =
            AttributeProviderFactory { _ ->
                AttributeProvider { node, tagName, attributes ->
                    if (node is FencedCodeBlock && tagName == "code") {
                        // Remove the default attribute for info
                        attributes.remove("class")
                        // Put info in custom attribute instead
                        attributes["data-custom"] = node.info ?: ""
                    } else if (node is FencedCodeBlock && tagName == "pre") {
                        attributes["data-code-block"] = "fenced"
                    }
                }
            }

        val renderer = HtmlRenderer.builder().attributeProviderFactory(custom).build()
        val rendered = renderer.render(parse("```info\ncontent\n```"))
        assertEquals("<pre data-code-block=\"fenced\"><code data-custom=\"info\">content\n</code></pre>\n", rendered)

        val rendered2 = renderer.render(parse("```evil\"\ncontent\n```"))
        assertEquals("<pre data-code-block=\"fenced\"><code data-custom=\"evil&quot;\">content\n</code></pre>\n", rendered2)
    }

    @Test
    fun attributeProviderForImage() {
        val custom =
            AttributeProviderFactory { _ ->
                AttributeProvider { node, _, attributes ->
                    if (node is Image) {
                        attributes.remove("alt")
                        attributes["test"] = "hey"
                    }
                }
            }

        val renderer = HtmlRenderer.builder().attributeProviderFactory(custom).build()
        val rendered = renderer.render(parse("![foo](/url)\n"))
        assertEquals("<p><img src=\"/url\" test=\"hey\" /></p>\n", rendered)
    }

    @Test
    fun attributeProviderFactoryNewInstanceForEachRender() {
        val factory =
            AttributeProviderFactory { _ ->
                object : AttributeProvider {
                    var i = 0

                    override fun setAttributes(
                        node: Node,
                        tagName: String,
                        attributes: MutableMap<String, String?>,
                    ) {
                        attributes["key"] = "$i"
                        i++
                    }
                }
            }

        val renderer = HtmlRenderer.builder().attributeProviderFactory(factory).build()
        val rendered = renderer.render(parse("text node"))
        val secondPass = renderer.render(parse("text node"))
        assertEquals(rendered, secondPass)
    }

    @Test
    fun overrideNodeRender() {
        val nodeRendererFactory =
            HtmlNodeRendererFactory { context ->
                object : NodeRenderer {
                    override fun getNodeTypes(): Set<KClass<out Node>> = setOf(Link::class)

                    override fun render(node: Node) {
                        context.getWriter().text("test")
                    }
                }
            }

        val renderer = HtmlRenderer.builder().nodeRendererFactory(nodeRendererFactory).build()
        val rendered = renderer.render(parse("foo [bar](/url)"))
        assertEquals("<p>foo test</p>\n", rendered)
    }

    @Test
    fun orderedListStartZero() {
        assertEquals("<ol start=\"0\">\n<li>Test</li>\n</ol>\n", defaultRenderer().render(parse("0. Test\n")))
    }

    @Test
    fun imageAltTextWithSoftLineBreak() {
        assertEquals("<p><img src=\"/url\" alt=\"foo\nbar\" /></p>\n", defaultRenderer().render(parse("![foo\nbar](/url)\n")))
    }

    @Test
    fun imageAltTextWithHardLineBreak() {
        assertEquals("<p><img src=\"/url\" alt=\"foo\nbar\" /></p>\n", defaultRenderer().render(parse("![foo  \nbar](/url)\n")))
    }

    @Test
    fun imageAltTextWithEntities() {
        assertEquals("<p><img src=\"/url\" alt=\"foo \u00E4\" /></p>\n", defaultRenderer().render(parse("![foo &auml;](/url)\n")))
    }

    @Test
    fun imageAltTextWithInlines() {
        assertEquals(
            "<p><img src=\"/url\" alt=\"foo bar link\" /></p>\n",
            defaultRenderer().render(parse("![_foo_ **bar** [link](/url)](/url)\n")),
        )
    }

    @Test
    fun imageAltTextWithCode() {
        assertEquals("<p><img src=\"/url\" alt=\"foo bar\" /></p>\n", defaultRenderer().render(parse("![`foo` bar](/url)\n")))
    }

    @Test
    fun canRenderContentsOfSingleParagraph() {
        val paragraphs = parse("Here I have a test [link](http://www.google.com)")
        val paragraph = paragraphs.firstChild

        val document = Document()
        var child = paragraph?.firstChild
        while (child != null) {
            val current = child
            child = current.next

            document.appendChild(current)
        }

        assertEquals("Here I have a test <a href=\"http://www.google.com\">link</a>", defaultRenderer().render(document))
    }

    @Test
    fun omitSingleParagraphP() {
        val renderer = HtmlRenderer.builder().omitSingleParagraphP(true).build()
        assertEquals("hi <em>there</em>", renderer.render(parse("hi *there*")))
    }

    companion object {
        private fun defaultRenderer(): HtmlRenderer = HtmlRenderer.builder().build()

        private fun htmlAllowingRenderer(): HtmlRenderer = HtmlRenderer.builder().escapeHtml(false).build()

        private fun sanitizeUrlsRenderer(): HtmlRenderer =
            HtmlRenderer
                .builder()
                .sanitizeUrls(true)
                .urlSanitizer(DefaultUrlSanitizer())
                .build()

        private fun rawUrlsRenderer(): HtmlRenderer = HtmlRenderer.builder().sanitizeUrls(false).build()

        private fun htmlEscapingRenderer(): HtmlRenderer = HtmlRenderer.builder().escapeHtml(true).build()

        private fun percentEncodingRenderer(): HtmlRenderer = HtmlRenderer.builder().percentEncodeUrls(true).build()

        private fun parse(source: String): Node = Parser.builder().build().parse(source)
    }
}
