package org.commonmark.ext.gfm.tables

import org.commonmark.Extension
import org.commonmark.node.Node
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.SourceSpan
import org.commonmark.node.Text
import org.commonmark.parser.IncludeSourceSpans
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TablesTest {
    private val extensions: Set<Extension> = setOf(TablesExtension.create())
    private val parser: Parser = Parser.builder().extensions(extensions).build()
    private val renderer: HtmlRenderer = HtmlRenderer.builder().extensions(extensions).build()

    private fun render(source: String): String = renderer.render(parser.parse(source))

    private fun assertRendering(
        source: String,
        expected: String,
    ) {
        val actual = render(source)
        val expectedWithSource = showTabs("$expected\n\n$source")
        val actualWithSource = showTabs("$actual\n\n$source")
        assertEquals(expectedWithSource, actualWithSource)
    }

    private fun showTabs(s: String): String = s.replace("\t", "\u2192")

    @Test
    fun mustHaveHeaderAndSeparator() {
        assertRendering("Abc|Def", "<p>Abc|Def</p>\n")
        assertRendering("Abc | Def", "<p>Abc | Def</p>\n")
    }

    @Test
    fun separatorMustBeOneOrMore() {
        assertRendering(
            "Abc|Def\n-|-",
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "</table>\n",
        )
        assertRendering(
            "Abc|Def\n--|--",
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "</table>\n",
        )
    }

    @Test
    fun separatorMustNotContainInvalidChars() {
        assertRendering("Abc|Def\n |-a-|---", "<p>Abc|Def\n|-a-|---</p>\n")
        assertRendering("Abc|Def\n |:--a|---", "<p>Abc|Def\n|:--a|---</p>\n")
        assertRendering("Abc|Def\n |:--a--:|---", "<p>Abc|Def\n|:--a--:|---</p>\n")
    }

    @Test
    fun separatorCanHaveLeadingSpaceThenPipe() {
        assertRendering(
            "Abc|Def\n |---|---",
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "</table>\n",
        )
    }

    @Test
    fun separatorCanNotHaveAdjacentPipes() {
        assertRendering("Abc|Def\n---||---", "<p>Abc|Def\n---||---</p>\n")
    }

    @Test
    fun separatorNeedsPipes() {
        assertRendering("Abc|Def\n|--- ---", "<p>Abc|Def\n|--- ---</p>\n")
    }

    @Test
    fun oneHeadNoBody() {
        assertRendering(
            "Abc|Def\n---|---",
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "</table>\n",
        )
    }

    @Test
    fun oneColumnOneHeadNoBody() {
        val expected =
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "</table>\n"
        assertRendering("|Abc\n|---\n", expected)
        assertRendering("|Abc|\n|---|\n", expected)
        assertRendering("Abc|\n---|\n", expected)

        // Pipe required on separator
        assertRendering("|Abc\n---\n", "<h2>|Abc</h2>\n")
        // Pipe required on head
        assertRendering("Abc\n|---\n", "<p>Abc\n|---</p>\n")
    }

    @Test
    fun oneColumnOneHeadOneBody() {
        val expected =
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n"
        assertRendering("|Abc\n|---\n|1", expected)
        assertRendering("|Abc|\n|---|\n|1|", expected)
        assertRendering("Abc|\n---|\n1|", expected)

        // Pipe required on separator
        assertRendering("|Abc\n---\n|1", "<h2>|Abc</h2>\n<p>|1</p>\n")
    }

    @Test
    fun oneHeadOneBody() {
        assertRendering(
            "Abc|Def\n---|---\n1|2",
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n",
        )
    }

    @Test
    fun spaceBeforeSeparator() {
        assertRendering(
            "  |Abc|Def|\n  |---|---|\n  |1|2|",
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n",
        )
    }

    @Test
    fun separatorMustNotHaveLessPartsThanHead() {
        assertRendering("Abc|Def|Ghi\n---|---\n1|2|3", "<p>Abc|Def|Ghi\n---|---\n1|2|3</p>\n")
    }

    @Test
    fun padding() {
        assertRendering(
            " Abc  | Def \n --- | --- \n 1 | 2 ",
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n",
        )
    }

    @Test
    fun paddingWithCodeBlockIndentation() {
        assertRendering(
            "Abc|Def\n---|---\n    1|2",
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n",
        )
    }

    @Test
    fun pipesOnOutside() {
        assertRendering(
            "|Abc|Def|\n|---|---|\n|1|2|",
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n",
        )
    }

    @Test
    fun pipesOnOutsideWhitespaceAfterHeader() {
        assertRendering(
            "|Abc|Def| \n|---|---|\n|1|2|",
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n",
        )
    }

    @Test
    fun pipesOnOutsideZeroLengthHeaders() {
        assertRendering(
            "||center header||\n" +
                "-|-------------|-\n" +
                "1|      2      |3",
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th></th>\n" +
                "<th>center header</th>\n" +
                "<th></th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "<td>3</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n",
        )
    }

    @Test
    fun inlineElements() {
        assertRendering(
            "*Abc*|Def\n---|---\n1|2",
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th><em>Abc</em></th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n",
        )
    }

    @Test
    fun escapedPipe() {
        assertRendering(
            "Abc|Def\n---|---\n1\\|2|20",
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1|2</td>\n" +
                "<td>20</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n",
        )
    }

    @Test
    fun escapedBackslash() {
        assertRendering(
            "Abc|Def\n---|---\n1\\\\|2",
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1|2</td>\n" +
                "<td></td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n",
        )
    }

    @Test
    fun escapedOther() {
        assertRendering(
            "Abc|Def\n---|---\n1|\\`not code`",
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>`not code`</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n",
        )
    }

    @Test
    fun backslashAtEnd() {
        assertRendering(
            "Abc|Def\n---|---\n1|2\\",
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2\\</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n",
        )
    }

    @Test
    fun alignLeft() {
        val expected =
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th align=\"left\">Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td align=\"left\">1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n"
        assertRendering("Abc|Def\n:-|-\n1|2", expected)
        assertRendering("Abc|Def\n:-|-\n1|2", expected)
        assertRendering("Abc|Def\n:---|---\n1|2", expected)
    }

    @Test
    fun alignRight() {
        val expected =
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th align=\"right\">Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td align=\"right\">1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n"
        assertRendering("Abc|Def\n-:|-\n1|2", expected)
        assertRendering("Abc|Def\n--:|--\n1|2", expected)
        assertRendering("Abc|Def\n---:|---\n1|2", expected)
    }

    @Test
    fun alignCenter() {
        val expected =
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th align=\"center\">Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td align=\"center\">1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n"
        assertRendering("Abc|Def\n:-:|-\n1|2", expected)
        assertRendering("Abc|Def\n:--:|--\n1|2", expected)
        assertRendering("Abc|Def\n:---:|---\n1|2", expected)
    }

    @Test
    fun alignCenterSecond() {
        assertRendering(
            "Abc|Def\n---|:---:\n1|2",
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th align=\"center\">Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td align=\"center\">2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n",
        )
    }

    @Test
    fun alignLeftWithSpaces() {
        assertRendering(
            "Abc|Def\n :--- |---\n1|2",
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th align=\"left\">Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td align=\"left\">1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n",
        )
    }

    @Test
    fun alignmentMarkerMustBeNextToDashes() {
        assertRendering("Abc|Def\n: ---|---", "<p>Abc|Def\n: ---|---</p>\n")
        assertRendering("Abc|Def\n--- :|---", "<p>Abc|Def\n--- :|---</p>\n")
        assertRendering("Abc|Def\n---|: ---", "<p>Abc|Def\n---|: ---</p>\n")
        assertRendering("Abc|Def\n---|--- :", "<p>Abc|Def\n---|--- :</p>\n")
    }

    @Test
    fun bodyCanNotHaveMoreColumnsThanHead() {
        assertRendering(
            "Abc|Def\n---|---\n1|2|3",
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n",
        )
    }

    @Test
    fun bodyWithFewerColumnsThanHeadResultsInEmptyCells() {
        assertRendering(
            "Abc|Def|Ghi\n---|---|---\n1|2",
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "<th>Ghi</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "<td></td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n",
        )
    }

    @Test
    fun insideBlockQuote() {
        assertRendering(
            "> Abc|Def\n> ---|---\n> 1|2",
            "<blockquote>\n" +
                "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "</blockquote>\n",
        )
    }

    @Test
    fun tableWithLazyContinuationLine() {
        assertRendering(
            "Abc|Def\n---|---\n1|2\nlazy",
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td>lazy</td>\n" +
                "<td></td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n",
        )
    }

    @Test
    fun issue142() {
        assertRendering(
            "||Alveolar|Bilabial\n" +
                "|:--|:-:|:-:\n" +
                "|**Plosive**|t, d|b\n" +
                "|**Tap**|\u0279|",
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th align=\"left\"></th>\n" +
                "<th align=\"center\">Alveolar</th>\n" +
                "<th align=\"center\">Bilabial</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td align=\"left\"><strong>Plosive</strong></td>\n" +
                "<td align=\"center\">t, d</td>\n" +
                "<td align=\"center\">b</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td align=\"left\"><strong>Tap</strong></td>\n" +
                "<td align=\"center\">\u0279</td>\n" +
                "<td align=\"center\"></td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n",
        )
    }

    @Test
    fun danglingPipe() {
        assertRendering(
            "Abc|Def\n" +
                "---|---\n" +
                "1|2\n" +
                "|",
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "<p>|</p>\n",
        )

        assertRendering(
            "Abc|Def\n" +
                "---|---\n" +
                "1|2\n" +
                "  |  ",
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "<p>|</p>\n",
        )
    }

    @Test
    fun interruptsParagraph() {
        assertRendering(
            "text\n" +
                "|a  |\n" +
                "|---|\n" +
                "|b  |",
            "<p>text</p>\n" +
                "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>a</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>b</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n",
        )
    }

    @Test
    fun attributeProviderIsApplied() {
        val factory = { _: org.commonmark.renderer.html.AttributeProviderContext ->
            org.commonmark.renderer.html.AttributeProvider { node, tagName, attributes ->
                when (node) {
                    is TableBlock -> attributes["test"] = "block"
                    is TableHead -> attributes["test"] = "head"
                    is TableBody -> attributes["test"] = "body"
                    is TableRow -> attributes["test"] = "row"
                    is TableCell -> attributes["test"] = "cell"
                }
            }
        }
        val htmlRenderer =
            HtmlRenderer
                .builder()
                .attributeProviderFactory(factory)
                .extensions(extensions)
                .build()
        val rendered = htmlRenderer.render(parser.parse("Abc|Def\n---|---\n1|2"))
        assertEquals(
            "<table test=\"block\">\n" +
                "<thead test=\"head\">\n" +
                "<tr test=\"row\">\n" +
                "<th test=\"cell\">Abc</th>\n" +
                "<th test=\"cell\">Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody test=\"body\">\n" +
                "<tr test=\"row\">\n" +
                "<td test=\"cell\">1</td>\n" +
                "<td test=\"cell\">2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n",
            rendered,
        )
    }

    @Test
    fun columnWidthIsRecorded() {
        val factory = { _: org.commonmark.renderer.html.AttributeProviderContext ->
            org.commonmark.renderer.html.AttributeProvider { node, tagName, attributes ->
                if (node is TableCell && tagName == "th") {
                    attributes["width"] = "${node.width}em"
                }
            }
        }
        val htmlRenderer =
            HtmlRenderer
                .builder()
                .attributeProviderFactory(factory)
                .extensions(extensions)
                .build()
        val rendered = htmlRenderer.render(parser.parse("Abc|Def\n-----|---\n1|2"))
        assertEquals(
            "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th width=\"5em\">Abc</th>\n" +
                "<th width=\"3em\">Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n",
            rendered,
        )
    }

    @Test
    fun sourceSpans() {
        val sourceParser =
            Parser
                .builder()
                .extensions(extensions)
                .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
                .build()
        val document = sourceParser.parse("Abc|Def\n---|---\n|1|2\n 3|four|\n|||\n")

        val block = document.firstChild as TableBlock
        assertEquals(
            listOf(
                SourceSpan.of(0, 0, 0, 7),
                SourceSpan.of(1, 0, 8, 7),
                SourceSpan.of(2, 0, 16, 4),
                SourceSpan.of(3, 0, 21, 8),
                SourceSpan.of(4, 0, 30, 3),
            ),
            block.getSourceSpans(),
        )

        val head = block.firstChild as TableHead
        assertEquals(listOf(SourceSpan.of(0, 0, 0, 7)), head.getSourceSpans())

        val headRow = head.firstChild as TableRow
        assertEquals(listOf(SourceSpan.of(0, 0, 0, 7)), headRow.getSourceSpans())
        val headRowCell1 = headRow.firstChild as TableCell
        val headRowCell2 = headRow.lastChild as TableCell
        assertEquals(listOf(SourceSpan.of(0, 0, 0, 3)), headRowCell1.getSourceSpans())
        assertEquals(listOf(SourceSpan.of(0, 0, 0, 3)), headRowCell1.firstChild!!.getSourceSpans())
        assertEquals(listOf(SourceSpan.of(0, 4, 4, 3)), headRowCell2.getSourceSpans())
        assertEquals(listOf(SourceSpan.of(0, 4, 4, 3)), headRowCell2.firstChild!!.getSourceSpans())

        val body = block.lastChild as TableBody
        assertEquals(
            listOf(SourceSpan.of(2, 0, 16, 4), SourceSpan.of(3, 0, 21, 8), SourceSpan.of(4, 0, 30, 3)),
            body.getSourceSpans(),
        )

        val bodyRow1 = body.firstChild as TableRow
        assertEquals(listOf(SourceSpan.of(2, 0, 16, 4)), bodyRow1.getSourceSpans())
        val bodyRow1Cell1 = bodyRow1.firstChild as TableCell
        val bodyRow1Cell2 = bodyRow1.lastChild as TableCell
        assertEquals(listOf(SourceSpan.of(2, 1, 17, 1)), bodyRow1Cell1.getSourceSpans())
        assertEquals(listOf(SourceSpan.of(2, 1, 17, 1)), bodyRow1Cell1.firstChild!!.getSourceSpans())
        assertEquals(listOf(SourceSpan.of(2, 3, 19, 1)), bodyRow1Cell2.getSourceSpans())
        assertEquals(listOf(SourceSpan.of(2, 3, 19, 1)), bodyRow1Cell2.firstChild!!.getSourceSpans())

        val bodyRow2 = body.firstChild!!.next as TableRow
        assertEquals(listOf(SourceSpan.of(3, 0, 21, 8)), bodyRow2.getSourceSpans())
        val bodyRow2Cell1 = bodyRow2.firstChild as TableCell
        val bodyRow2Cell2 = bodyRow2.lastChild as TableCell
        assertEquals(listOf(SourceSpan.of(3, 1, 22, 1)), bodyRow2Cell1.getSourceSpans())
        assertEquals(listOf(SourceSpan.of(3, 1, 22, 1)), bodyRow2Cell1.firstChild!!.getSourceSpans())
        assertEquals(listOf(SourceSpan.of(3, 3, 24, 4)), bodyRow2Cell2.getSourceSpans())
        assertEquals(listOf(SourceSpan.of(3, 3, 24, 4)), bodyRow2Cell2.firstChild!!.getSourceSpans())

        val bodyRow3 = body.lastChild as TableRow
        assertEquals(listOf(SourceSpan.of(4, 0, 30, 3)), bodyRow3.getSourceSpans())
        val bodyRow3Cell1 = bodyRow3.firstChild as TableCell
        val bodyRow3Cell2 = bodyRow3.lastChild as TableCell
        assertEquals(emptyList(), bodyRow3Cell1.getSourceSpans())
        assertEquals(emptyList(), bodyRow3Cell2.getSourceSpans())
    }

    @Test
    fun sourceSpansWhenInterrupting() {
        val sourceParser =
            Parser
                .builder()
                .extensions(extensions)
                .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
                .build()
        val document =
            sourceParser.parse(
                "a\n" +
                    "bc\n" +
                    "|de|\n" +
                    "|---|\n" +
                    "|fg|",
            )

        val paragraph = document.firstChild as Paragraph
        val text = paragraph.firstChild as Text
        assertEquals("a", text.literal)
        assertIs<SoftLineBreak>(text.next)
        val text2 = text.next!!.next as Text
        assertEquals("bc", text2.literal)

        assertEquals(
            listOf(
                SourceSpan.of(0, 0, 0, 1),
                SourceSpan.of(1, 0, 2, 2),
            ),
            paragraph.getSourceSpans(),
        )

        val table = document.lastChild as TableBlock
        assertEquals(
            listOf(
                SourceSpan.of(2, 0, 5, 4),
                SourceSpan.of(3, 0, 10, 5),
                SourceSpan.of(4, 0, 16, 4),
            ),
            table.getSourceSpans(),
        )
    }
}
