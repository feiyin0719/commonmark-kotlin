package org.commonmark.ext.gfm.tables

import org.commonmark.Extension
import org.commonmark.parser.Parser
import org.commonmark.renderer.text.LineBreakRendering
import org.commonmark.renderer.text.TextContentRenderer
import kotlin.test.Test
import kotlin.test.assertEquals

class TablesTextContentTest {

    private val extensions: Set<Extension> = setOf(TablesExtension.create())
    private val parser: Parser = Parser.builder().extensions(extensions).build()

    private val compactRenderer: TextContentRenderer = TextContentRenderer.builder().extensions(extensions).build()
    private val separateRenderer: TextContentRenderer = TextContentRenderer.builder().extensions(extensions)
        .lineBreakRendering(LineBreakRendering.SEPARATE_BLOCKS).build()
    private val strippedRenderer: TextContentRenderer = TextContentRenderer.builder().extensions(extensions)
        .lineBreakRendering(LineBreakRendering.STRIP).build()

    private fun assertCompact(source: String, expected: String) {
        val doc = parser.parse(source)
        val actualRendering = compactRenderer.render(doc)
        assertEquals(expected, actualRendering, "Compact rendering mismatch for source:\n$source")
    }

    private fun assertSeparate(source: String, expected: String) {
        val doc = parser.parse(source)
        val actualRendering = separateRenderer.render(doc)
        assertEquals(expected, actualRendering, "Separate rendering mismatch for source:\n$source")
    }

    private fun assertStripped(source: String, expected: String) {
        val doc = parser.parse(source)
        val actualRendering = strippedRenderer.render(doc)
        assertEquals(expected, actualRendering, "Stripped rendering mismatch for source:\n$source")
    }

    @Test
    fun oneHeadNoBody() {
        assertCompact("Abc|Def\n---|---", "Abc| Def")
    }

    @Test
    fun oneColumnOneHeadNoBody() {
        val expected = "Abc"
        assertCompact("|Abc\n|---\n", expected)
        assertCompact("|Abc|\n|---|\n", expected)
        assertCompact("Abc|\n---|\n", expected)

        // Pipe required on separator
        assertCompact("|Abc\n---\n", "|Abc")
        // Pipe required on head
        assertCompact("Abc\n|---\n", "Abc\n|---")
    }

    @Test
    fun oneColumnOneHeadOneBody() {
        val expected = "Abc\n1"
        assertCompact("|Abc\n|---\n|1", expected)
        assertCompact("|Abc|\n|---|\n|1|", expected)
        assertCompact("Abc|\n---|\n1|", expected)

        // Pipe required on separator
        assertCompact("|Abc\n---\n|1", "|Abc\n|1")
    }

    @Test
    fun oneHeadOneBody() {
        assertCompact("Abc|Def\n---|---\n1|2", "Abc| Def\n1| 2")
    }

    @Test
    fun separatorMustNotHaveLessPartsThanHead() {
        assertCompact("Abc|Def|Ghi\n---|---\n1|2|3", "Abc|Def|Ghi\n---|---\n1|2|3")
    }

    @Test
    fun padding() {
        assertCompact(" Abc  | Def \n --- | --- \n 1 | 2 ", "Abc| Def\n1| 2")
    }

    @Test
    fun paddingWithCodeBlockIndentation() {
        assertCompact("Abc|Def\n---|---\n    1|2", "Abc| Def\n1| 2")
    }

    @Test
    fun pipesOnOutside() {
        assertCompact("|Abc|Def|\n|---|---|\n|1|2|", "Abc| Def\n1| 2")
    }

    @Test
    fun inlineElements() {
        assertCompact("*Abc*|Def\n---|---\n1|2", "Abc| Def\n1| 2")
    }

    @Test
    fun escapedPipe() {
        assertCompact("Abc|Def\n---|---\n1\\|2|20", "Abc| Def\n1|2| 20")
    }

    @Test
    fun alignLeft() {
        assertCompact("Abc|Def\n:---|---\n1|2", "Abc| Def\n1| 2")
    }

    @Test
    fun alignRight() {
        assertCompact("Abc|Def\n---:|---\n1|2", "Abc| Def\n1| 2")
    }

    @Test
    fun alignCenter() {
        assertCompact("Abc|Def\n:---:|---\n1|2", "Abc| Def\n1| 2")
    }

    @Test
    fun alignCenterSecond() {
        assertCompact("Abc|Def\n---|:---:\n1|2", "Abc| Def\n1| 2")
    }

    @Test
    fun alignLeftWithSpaces() {
        assertCompact("Abc|Def\n :--- |---\n1|2", "Abc| Def\n1| 2")
    }

    @Test
    fun alignmentMarkerMustBeNextToDashes() {
        assertCompact("Abc|Def\n: ---|---", "Abc|Def\n: ---|---")
        assertCompact("Abc|Def\n--- :|---", "Abc|Def\n--- :|---")
        assertCompact("Abc|Def\n---|: ---", "Abc|Def\n---|: ---")
        assertCompact("Abc|Def\n---|--- :", "Abc|Def\n---|--- :")
    }

    @Test
    fun bodyCanNotHaveMoreColumnsThanHead() {
        assertCompact("Abc|Def\n---|---\n1|2|3", "Abc| Def\n1| 2")
    }

    @Test
    fun bodyWithFewerColumnsThanHeadResultsInEmptyCells() {
        assertCompact("Abc|Def|Ghi\n---|---|---\n1|2", "Abc| Def| Ghi\n1| 2| ")
    }

    @Test
    fun insideBlockQuote() {
        assertCompact("> Abc|Def\n> ---|---\n> 1|2", "\u00abAbc| Def\n1| 2\u00bb")
    }

    @Test
    fun tableWithLazyContinuationLine() {
        assertCompact("Abc|Def\n---|---\n1|2\nlazy", "Abc| Def\n1| 2\nlazy| ")
    }

    @Test
    fun tableBetweenOtherBlocks() {
        val s = "Foo\n\nAbc|Def\n---|---\n1|2\n\nBar"
        assertCompact(s, "Foo\nAbc| Def\n1| 2\nBar")
        assertSeparate(s, "Foo\n\nAbc| Def\n1| 2\n\nBar")
        assertStripped(s, "Foo Abc| Def 1| 2 Bar")
    }
}
