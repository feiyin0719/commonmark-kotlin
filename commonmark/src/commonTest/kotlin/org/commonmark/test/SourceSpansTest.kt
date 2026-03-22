package org.commonmark.test

import org.commonmark.node.*
import org.commonmark.parser.IncludeSourceSpans
import org.commonmark.parser.Parser
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals

class SourceSpansTest {
    @Test
    fun paragraph() {
        assertSpans("foo\n", Paragraph::class, SourceSpan.of(0, 0, 0, 3))
        assertSpans("foo\nbar\n", Paragraph::class, SourceSpan.of(0, 0, 0, 3), SourceSpan.of(1, 0, 4, 3))
        assertSpans("  foo\n  bar\n", Paragraph::class, SourceSpan.of(0, 0, 0, 5), SourceSpan.of(1, 0, 6, 5))
        assertSpans("> foo\n> bar\n", Paragraph::class, SourceSpan.of(0, 2, 2, 3), SourceSpan.of(1, 2, 8, 3))
        assertSpans("* foo\n  bar\n", Paragraph::class, SourceSpan.of(0, 2, 2, 3), SourceSpan.of(1, 2, 8, 3))
        assertSpans("* foo\nbar\n", Paragraph::class, SourceSpan.of(0, 2, 2, 3), SourceSpan.of(1, 0, 6, 3))
    }

    @Test
    fun thematicBreak() {
        assertSpans("---\n", ThematicBreak::class, SourceSpan.of(0, 0, 0, 3))
        assertSpans("  ---\n", ThematicBreak::class, SourceSpan.of(0, 0, 0, 5))
        assertSpans("> ---\n", ThematicBreak::class, SourceSpan.of(0, 2, 2, 3))
    }

    @Test
    fun atxHeading() {
        assertSpans("# foo", Heading::class, SourceSpan.of(0, 0, 0, 5))
        assertSpans(" # foo", Heading::class, SourceSpan.of(0, 0, 0, 6))
        assertSpans("## foo ##", Heading::class, SourceSpan.of(0, 0, 0, 9))
        assertSpans("> # foo", Heading::class, SourceSpan.of(0, 2, 2, 5))
    }

    @Test
    fun setextHeading() {
        assertSpans("foo\n===\n", Heading::class, SourceSpan.of(0, 0, 0, 3), SourceSpan.of(1, 0, 4, 3))
        assertSpans("foo\nbar\n====\n", Heading::class, SourceSpan.of(0, 0, 0, 3), SourceSpan.of(1, 0, 4, 3), SourceSpan.of(2, 0, 8, 4))
        assertSpans("  foo\n  ===\n", Heading::class, SourceSpan.of(0, 0, 0, 5), SourceSpan.of(1, 0, 6, 5))
        assertSpans("> foo\n> ===\n", Heading::class, SourceSpan.of(0, 2, 2, 3), SourceSpan.of(1, 2, 8, 3))
    }

    @Test
    fun indentedCodeBlock() {
        assertSpans("    foo\n", IndentedCodeBlock::class, SourceSpan.of(0, 0, 0, 7))
        assertSpans("     foo\n", IndentedCodeBlock::class, SourceSpan.of(0, 0, 0, 8))
        assertSpans("\tfoo\n", IndentedCodeBlock::class, SourceSpan.of(0, 0, 0, 4))
        assertSpans(" \tfoo\n", IndentedCodeBlock::class, SourceSpan.of(0, 0, 0, 5))
        assertSpans("  \tfoo\n", IndentedCodeBlock::class, SourceSpan.of(0, 0, 0, 6))
        assertSpans("   \tfoo\n", IndentedCodeBlock::class, SourceSpan.of(0, 0, 0, 7))
        assertSpans("    \tfoo\n", IndentedCodeBlock::class, SourceSpan.of(0, 0, 0, 8))
        assertSpans("    \t foo\n", IndentedCodeBlock::class, SourceSpan.of(0, 0, 0, 9))
        assertSpans("\t foo\n", IndentedCodeBlock::class, SourceSpan.of(0, 0, 0, 5))
        assertSpans("\t  foo\n", IndentedCodeBlock::class, SourceSpan.of(0, 0, 0, 6))
        assertSpans("    foo\n     bar\n", IndentedCodeBlock::class, SourceSpan.of(0, 0, 0, 7), SourceSpan.of(1, 0, 8, 8))
        assertSpans("    foo\n\tbar\n", IndentedCodeBlock::class, SourceSpan.of(0, 0, 0, 7), SourceSpan.of(1, 0, 8, 4))
        assertSpans(
            "    foo\n    \n     \n",
            IndentedCodeBlock::class,
            SourceSpan.of(0, 0, 0, 7),
            SourceSpan.of(1, 0, 8, 4),
            SourceSpan.of(2, 0, 13, 5),
        )
        assertSpans(">     foo\n", IndentedCodeBlock::class, SourceSpan.of(0, 2, 2, 7))
    }

    @Test
    fun fencedCodeBlock() {
        assertSpans(
            "```\nfoo\n```\n",
            FencedCodeBlock::class,
            SourceSpan.of(0, 0, 0, 3),
            SourceSpan.of(1, 0, 4, 3),
            SourceSpan.of(2, 0, 8, 3),
        )
        assertSpans(
            "```\n foo\n```\n",
            FencedCodeBlock::class,
            SourceSpan.of(0, 0, 0, 3),
            SourceSpan.of(1, 0, 4, 4),
            SourceSpan.of(2, 0, 9, 3),
        )
        assertSpans(
            "```\nfoo\nbar\n```\n",
            FencedCodeBlock::class,
            SourceSpan.of(0, 0, 0, 3),
            SourceSpan.of(1, 0, 4, 3),
            SourceSpan.of(2, 0, 8, 3),
            SourceSpan.of(3, 0, 12, 3),
        )
        assertSpans(
            "   ```\n   foo\n   ```\n",
            FencedCodeBlock::class,
            SourceSpan.of(0, 0, 0, 6),
            SourceSpan.of(1, 0, 7, 6),
            SourceSpan.of(2, 0, 14, 6),
        )
        assertSpans(
            " ```\n foo\nfoo\n```\n",
            FencedCodeBlock::class,
            SourceSpan.of(0, 0, 0, 4),
            SourceSpan.of(1, 0, 5, 4),
            SourceSpan.of(2, 0, 10, 3),
            SourceSpan.of(3, 0, 14, 3),
        )
        assertSpans(
            "```info\nfoo\n```\n",
            FencedCodeBlock::class,
            SourceSpan.of(0, 0, 0, 7),
            SourceSpan.of(1, 0, 8, 3),
            SourceSpan.of(2, 0, 12, 3),
        )
        assertSpans(
            "* ```\n  foo\n  ```\n",
            FencedCodeBlock::class,
            SourceSpan.of(0, 2, 2, 3),
            SourceSpan.of(1, 2, 8, 3),
            SourceSpan.of(2, 2, 14, 3),
        )
        assertSpans(
            "> ```\n> foo\n> ```\n",
            FencedCodeBlock::class,
            SourceSpan.of(0, 2, 2, 3),
            SourceSpan.of(1, 2, 8, 3),
            SourceSpan.of(2, 2, 14, 3),
        )

        val document = PARSER.parse("```\nfoo\n```\nbar\n")
        val paragraph = document.lastChild as Paragraph
        assertEquals(listOf(SourceSpan.of(3, 0, 12, 3)), paragraph.getSourceSpans())
    }

    @Test
    fun htmlBlock() {
        assertSpans("<div>\n", HtmlBlock::class, SourceSpan.of(0, 0, 0, 5))
        assertSpans(
            " <div>\n foo\n </div>\n",
            HtmlBlock::class,
            SourceSpan.of(0, 0, 0, 6),
            SourceSpan.of(1, 0, 7, 4),
            SourceSpan.of(2, 0, 12, 7),
        )
        assertSpans("* <div>\n", HtmlBlock::class, SourceSpan.of(0, 2, 2, 5))
    }

    @Test
    fun blockQuote() {
        assertSpans(">foo\n", BlockQuote::class, SourceSpan.of(0, 0, 0, 4))
        assertSpans("> foo\n", BlockQuote::class, SourceSpan.of(0, 0, 0, 5))
        assertSpans(">  foo\n", BlockQuote::class, SourceSpan.of(0, 0, 0, 6))
        assertSpans(" > foo\n", BlockQuote::class, SourceSpan.of(0, 0, 0, 6))
        assertSpans("   > foo\n  > bar\n", BlockQuote::class, SourceSpan.of(0, 0, 0, 8), SourceSpan.of(1, 0, 9, 7))
        // Lazy continuations
        assertSpans("> foo\nbar\n", BlockQuote::class, SourceSpan.of(0, 0, 0, 5), SourceSpan.of(1, 0, 6, 3))
        assertSpans(
            "> foo\nbar\n> baz\n",
            BlockQuote::class,
            SourceSpan.of(0, 0, 0, 5),
            SourceSpan.of(1, 0, 6, 3),
            SourceSpan.of(2, 0, 10, 5),
        )
        assertSpans("> > foo\nbar\n", BlockQuote::class, SourceSpan.of(0, 0, 0, 7), SourceSpan.of(1, 0, 8, 3))
    }

    @Test
    fun listBlock() {
        assertSpans("* foo\n", ListBlock::class, SourceSpan.of(0, 0, 0, 5))
        assertSpans("* foo\n  bar\n", ListBlock::class, SourceSpan.of(0, 0, 0, 5), SourceSpan.of(1, 0, 6, 5))
        assertSpans("* foo\n* bar\n", ListBlock::class, SourceSpan.of(0, 0, 0, 5), SourceSpan.of(1, 0, 6, 5))
        assertSpans("* foo\n  # bar\n", ListBlock::class, SourceSpan.of(0, 0, 0, 5), SourceSpan.of(1, 0, 6, 7))
        assertSpans("* foo\n  * bar\n", ListBlock::class, SourceSpan.of(0, 0, 0, 5), SourceSpan.of(1, 0, 6, 7))
        assertSpans("* foo\n> bar\n", ListBlock::class, SourceSpan.of(0, 0, 0, 5))
        assertSpans("> * foo\n", ListBlock::class, SourceSpan.of(0, 2, 2, 5))

        // Lazy continuations
        assertSpans("* foo\nbar\nbaz", ListBlock::class, SourceSpan.of(0, 0, 0, 5), SourceSpan.of(1, 0, 6, 3), SourceSpan.of(2, 0, 10, 3))
        assertSpans("* foo\nbar\n* baz", ListBlock::class, SourceSpan.of(0, 0, 0, 5), SourceSpan.of(1, 0, 6, 3), SourceSpan.of(2, 0, 10, 5))
        assertSpans(
            "* foo\n  * bar\nbaz",
            ListBlock::class,
            SourceSpan.of(0, 0, 0, 5),
            SourceSpan.of(1, 0, 6, 7),
            SourceSpan.of(2, 0, 14, 3),
        )

        val document = PARSER.parse("* foo\n  * bar\n")
        val listBlock = document.firstChild!!.firstChild!!.lastChild as ListBlock
        assertEquals(listOf(SourceSpan.of(1, 2, 8, 5)), listBlock.getSourceSpans())
    }

    @Test
    fun listItem() {
        assertSpans("* foo\n", ListItem::class, SourceSpan.of(0, 0, 0, 5))
        assertSpans(" * foo\n", ListItem::class, SourceSpan.of(0, 0, 0, 6))
        assertSpans("  * foo\n", ListItem::class, SourceSpan.of(0, 0, 0, 7))
        assertSpans("   * foo\n", ListItem::class, SourceSpan.of(0, 0, 0, 8))
        assertSpans("*\n  foo\n", ListItem::class, SourceSpan.of(0, 0, 0, 1), SourceSpan.of(1, 0, 2, 5))
        assertSpans("*\n  foo\n  bar\n", ListItem::class, SourceSpan.of(0, 0, 0, 1), SourceSpan.of(1, 0, 2, 5), SourceSpan.of(2, 0, 8, 5))
        assertSpans("> * foo\n", ListItem::class, SourceSpan.of(0, 2, 2, 5))

        // Lazy continuations
        assertSpans("* foo\nbar\n", ListItem::class, SourceSpan.of(0, 0, 0, 5), SourceSpan.of(1, 0, 6, 3))
        assertSpans("* foo\nbar\nbaz\n", ListItem::class, SourceSpan.of(0, 0, 0, 5), SourceSpan.of(1, 0, 6, 3), SourceSpan.of(2, 0, 10, 3))
    }

    @Test
    fun linkReferenceDefinition() {
        // This is tricky due to how link reference definition parsing works. It is stripped from the paragraph if it's
        // successfully parsed, otherwise it stays part of the paragraph.
        val document = PARSER.parse("[foo]: /url\ntext\n")

        val linkReferenceDefinition = document.firstChild as LinkReferenceDefinition
        assertEquals(listOf(SourceSpan.of(0, 0, 0, 11)), linkReferenceDefinition.getSourceSpans())

        val paragraph = document.lastChild as Paragraph
        assertEquals(listOf(SourceSpan.of(1, 0, 12, 4)), paragraph.getSourceSpans())
    }

    @Test
    fun linkReferenceDefinitionMultiple() {
        val doc = PARSER.parse("[foo]: /foo\n[bar]: /bar\n")
        val def1 = doc.firstChild as LinkReferenceDefinition
        val def2 = doc.lastChild as LinkReferenceDefinition
        assertEquals(listOf(SourceSpan.of(0, 0, 0, 11)), def1.getSourceSpans())
        assertEquals(listOf(SourceSpan.of(1, 0, 12, 11)), def2.getSourceSpans())
    }

    @Test
    fun linkReferenceDefinitionWithTitle() {
        val doc = PARSER.parse("[1]: #not-code \"Text\"\n[foo]: /foo\n")
        val def1 = doc.firstChild as LinkReferenceDefinition
        val def2 = doc.lastChild as LinkReferenceDefinition
        assertEquals(listOf(SourceSpan.of(0, 0, 0, 21)), def1.getSourceSpans())
        assertEquals(listOf(SourceSpan.of(1, 0, 22, 11)), def2.getSourceSpans())
    }

    @Test
    fun linkReferenceDefinitionWithTitleInvalid() {
        val doc = PARSER.parse("[foo]: /url\n\"title\" ok\n")
        val def = TestNodes.find(doc, LinkReferenceDefinition::class)
        val paragraph = TestNodes.find(doc, Paragraph::class)
        assertEquals(listOf(SourceSpan.of(0, 0, 0, 11)), def.getSourceSpans())
        assertEquals(listOf(SourceSpan.of(1, 0, 12, 10)), paragraph.getSourceSpans())
    }

    @Test
    fun linkReferenceDefinitionHeading() {
        // This is probably the trickiest because we have a link reference definition at the start of a paragraph
        // that gets replaced because of a heading. Phew.
        val document = PARSER.parse("[foo]: /url\nHeading\n===\n")

        val linkReferenceDefinition = document.firstChild as LinkReferenceDefinition
        assertEquals(listOf(SourceSpan.of(0, 0, 0, 11)), linkReferenceDefinition.getSourceSpans())

        val heading = document.lastChild as Heading
        assertEquals(listOf(SourceSpan.of(1, 0, 12, 7), SourceSpan.of(2, 0, 20, 3)), heading.getSourceSpans())
    }

    @Test
    fun lazyContinuationLines() {
        run {
            // From https://spec.commonmark.org/0.31.2/#example-250
            // Wrong source span for the inner block quote for the second line.
            val doc = PARSER.parse("> > > foo\nbar\n")

            val bq1 = doc.lastChild as BlockQuote
            assertEquals(listOf(SourceSpan.of(0, 0, 0, 9), SourceSpan.of(1, 0, 10, 3)), bq1.getSourceSpans())
            val bq2 = bq1.lastChild as BlockQuote
            assertEquals(listOf(SourceSpan.of(0, 2, 2, 7), SourceSpan.of(1, 0, 10, 3)), bq2.getSourceSpans())
            val bq3 = bq2.lastChild as BlockQuote
            assertEquals(listOf(SourceSpan.of(0, 4, 4, 5), SourceSpan.of(1, 0, 10, 3)), bq3.getSourceSpans())
            val paragraph = bq3.lastChild as Paragraph
            assertEquals(listOf(SourceSpan.of(0, 6, 6, 3), SourceSpan.of(1, 0, 10, 3)), paragraph.getSourceSpans())
        }

        run {
            // Adding one character to the last line remove blockQuote3 source for the second line
            val doc = PARSER.parse("> > > foo\nbars\n")

            val bq1 = doc.lastChild as BlockQuote
            assertEquals(listOf(SourceSpan.of(0, 0, 0, 9), SourceSpan.of(1, 0, 10, 4)), bq1.getSourceSpans())
            val bq2 = bq1.lastChild as BlockQuote
            assertEquals(listOf(SourceSpan.of(0, 2, 2, 7), SourceSpan.of(1, 0, 10, 4)), bq2.getSourceSpans())
            val bq3 = bq2.lastChild as BlockQuote
            assertEquals(listOf(SourceSpan.of(0, 4, 4, 5), SourceSpan.of(1, 0, 10, 4)), bq3.getSourceSpans())
            val paragraph = bq3.lastChild as Paragraph
            assertEquals(listOf(SourceSpan.of(0, 6, 6, 3), SourceSpan.of(1, 0, 10, 4)), paragraph.getSourceSpans())
        }

        run {
            // From https://spec.commonmark.org/0.31.2/#example-292
            val doc = PARSER.parse("> 1. > Blockquote\ncontinued here.")

            val bq1 = doc.lastChild as BlockQuote
            assertEquals(listOf(SourceSpan.of(0, 0, 0, 17), SourceSpan.of(1, 0, 18, 15)), bq1.getSourceSpans())
            val orderedList = bq1.lastChild as OrderedList
            assertEquals(listOf(SourceSpan.of(0, 2, 2, 15), SourceSpan.of(1, 0, 18, 15)), orderedList.getSourceSpans())
            val listItem = orderedList.lastChild as ListItem
            assertEquals(listOf(SourceSpan.of(0, 2, 2, 15), SourceSpan.of(1, 0, 18, 15)), listItem.getSourceSpans())
            val bq2 = listItem.lastChild as BlockQuote
            assertEquals(listOf(SourceSpan.of(0, 5, 5, 12), SourceSpan.of(1, 0, 18, 15)), bq2.getSourceSpans())
            val paragraph = bq2.lastChild as Paragraph
            assertEquals(listOf(SourceSpan.of(0, 7, 7, 10), SourceSpan.of(1, 0, 18, 15)), paragraph.getSourceSpans())
        }

        run {
            // Lazy continuation line for nested blockquote
            val doc = PARSER.parse("> > foo\n> bar\n")

            val bq1 = doc.lastChild as BlockQuote
            assertEquals(listOf(SourceSpan.of(0, 0, 0, 7), SourceSpan.of(1, 0, 8, 5)), bq1.getSourceSpans())
            val bq2 = bq1.lastChild as BlockQuote
            assertEquals(listOf(SourceSpan.of(0, 2, 2, 5), SourceSpan.of(1, 2, 10, 3)), bq2.getSourceSpans())
            val paragraph = bq2.lastChild as Paragraph
            assertEquals(listOf(SourceSpan.of(0, 4, 4, 3), SourceSpan.of(1, 2, 10, 3)), paragraph.getSourceSpans())
        }
    }

    @Test
    fun visualCheck() {
        assertVisualize("> * foo\n>   bar\n> * baz\n", "(> {[* <foo>]})\n(> {[  <bar>]})\n(> {\u2E22* \u2E24baz\u2E25\u2E23})\n")
        assertVisualize("> * ```\n>   foo\n>   ```\n", "(> {[* <```>]})\n(> {[  <foo>]})\n(> {[  <```>]})\n")
    }

    @Test
    fun inlineText() {
        assertInlineSpans("foo", Text::class, SourceSpan.of(0, 0, 0, 3))
        assertInlineSpans("> foo", Text::class, SourceSpan.of(0, 2, 2, 3))
        assertInlineSpans("* foo", Text::class, SourceSpan.of(0, 2, 2, 3))

        // SourceSpans should be merged: ` is a separate Text node while inline parsing and gets merged at the end
        assertInlineSpans("foo`bar", Text::class, SourceSpan.of(0, 0, 0, 7))
        assertInlineSpans("foo[bar", Text::class, SourceSpan.of(0, 0, 0, 7))
        assertInlineSpans("> foo`bar", Text::class, SourceSpan.of(0, 2, 2, 7))

        assertInlineSpans("[foo](/url)", Text::class, SourceSpan.of(0, 1, 1, 3))
        assertInlineSpans("*foo*", Text::class, SourceSpan.of(0, 1, 1, 3))
    }

    @Test
    fun inlineHeading() {
        assertInlineSpans("# foo", Text::class, SourceSpan.of(0, 2, 2, 3))
        assertInlineSpans(" # foo", Text::class, SourceSpan.of(0, 3, 3, 3))
        assertInlineSpans("> # foo", Text::class, SourceSpan.of(0, 4, 4, 3))
    }

    @Test
    fun inlineAutolink() {
        assertInlineSpans("see <https://example.org>", Link::class, SourceSpan.of(0, 4, 4, 21))
    }

    @Test
    fun inlineBackslash() {
        assertInlineSpans("\\!", Text::class, SourceSpan.of(0, 0, 0, 2))
    }

    @Test
    fun inlineBackticks() {
        assertInlineSpans("see `code`", Code::class, SourceSpan.of(0, 4, 4, 6))
        assertInlineSpans(
            "`multi\nline`",
            Code::class,
            SourceSpan.of(0, 0, 0, 6),
            SourceSpan.of(1, 0, 7, 5),
        )
        assertInlineSpans("text ```", Text::class, SourceSpan.of(0, 0, 0, 8))
    }

    @Test
    fun inlineEntity() {
        assertInlineSpans("&amp;", Text::class, SourceSpan.of(0, 0, 0, 5))
    }

    @Test
    fun inlineHtml() {
        assertInlineSpans("hi <strong>there</strong>", HtmlInline::class, SourceSpan.of(0, 3, 3, 8))
    }

    @Test
    fun links() {
        assertInlineSpans("\n[text](/url)", Link::class, SourceSpan.of(1, 0, 1, 12))
        assertInlineSpans("\n[text](/url)", Text::class, SourceSpan.of(1, 1, 2, 4))

        assertInlineSpans("\n[text]\n\n[text]: /url", Link::class, SourceSpan.of(1, 0, 1, 6))
        assertInlineSpans("\n[text]\n\n[text]: /url", Text::class, SourceSpan.of(1, 1, 2, 4))
        assertInlineSpans("\n[text][]\n\n[text]: /url", Link::class, SourceSpan.of(1, 0, 1, 8))
        assertInlineSpans("\n[text][]\n\n[text]: /url", Text::class, SourceSpan.of(1, 1, 2, 4))
        assertInlineSpans("\n[text][ref]\n\n[ref]: /url", Link::class, SourceSpan.of(1, 0, 1, 11))
        assertInlineSpans("\n[text][ref]\n\n[ref]: /url", Text::class, SourceSpan.of(1, 1, 2, 4))
        assertInlineSpans("\n[notalink]", Text::class, SourceSpan.of(1, 0, 1, 10))
    }

    @Test
    fun inlineEmphasis() {
        assertInlineSpans("\n*hey*", Emphasis::class, SourceSpan.of(1, 0, 1, 5))
        assertInlineSpans("\n*hey*", Text::class, SourceSpan.of(1, 1, 2, 3))
        assertInlineSpans("\n**hey**", StrongEmphasis::class, SourceSpan.of(1, 0, 1, 7))
        assertInlineSpans("\n**hey**", Text::class, SourceSpan.of(1, 2, 3, 3))

        // This is an interesting one. It renders like this:
        // <p>*<em>hey</em></p>
        // The delimiter processor only uses one of the asterisks.
        // So the first Text node should be the `*` at the beginning with the correct span.
        assertInlineSpans("\n**hey*", Text::class, SourceSpan.of(1, 0, 1, 1))
        assertInlineSpans("\n**hey*", Emphasis::class, SourceSpan.of(1, 1, 2, 5))

        assertInlineSpans("\n***hey**", Text::class, SourceSpan.of(1, 0, 1, 1))
        assertInlineSpans("\n***hey**", StrongEmphasis::class, SourceSpan.of(1, 1, 2, 7))

        val document = INLINES_PARSER.parse("*hey**")
        val lastText = document.firstChild!!.lastChild!!
        assertEquals(listOf(SourceSpan.of(0, 5, 5, 1)), lastText.getSourceSpans())
    }

    @Test
    fun tabExpansion() {
        assertInlineSpans(">\tfoo", BlockQuote::class, SourceSpan.of(0, 0, 0, 5))
        assertInlineSpans(">\tfoo", Text::class, SourceSpan.of(0, 2, 2, 3))

        assertInlineSpans("a\tb", Text::class, SourceSpan.of(0, 0, 0, 3))
    }

    @Test
    fun differentLineTerminators() {
        val input = "foo\nbar\rbaz\r\nqux\r\n\r\n> *hi*"
        assertSpans(
            input,
            Paragraph::class,
            SourceSpan.of(0, 0, 0, 3),
            SourceSpan.of(1, 0, 4, 3),
            SourceSpan.of(2, 0, 8, 3),
            SourceSpan.of(3, 0, 13, 3),
        )
        assertSpans(
            input,
            BlockQuote::class,
            SourceSpan.of(5, 0, 20, 6),
        )

        assertInlineSpans(input, Emphasis::class, SourceSpan.of(5, 2, 22, 4))
    }

    private fun assertVisualize(
        source: String,
        expected: String,
    ) {
        val doc = PARSER.parse(source)
        assertEquals(expected, SourceSpanRenderer.renderWithLineColumn(doc, source))
        assertEquals(expected, SourceSpanRenderer.renderWithInputIndex(doc, source))
    }

    companion object {
        private val PARSER = Parser.builder().includeSourceSpans(IncludeSourceSpans.BLOCKS).build()
        private val INLINES_PARSER = Parser.builder().includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES).build()

        private fun assertSpans(
            input: String,
            nodeClass: KClass<out Node>,
            vararg expectedSourceSpans: SourceSpan,
        ) {
            assertSpansFromDocument(PARSER.parse(input), nodeClass, *expectedSourceSpans)
        }

        private fun assertInlineSpans(
            input: String,
            nodeClass: KClass<out Node>,
            vararg expectedSourceSpans: SourceSpan,
        ) {
            assertSpansFromDocument(INLINES_PARSER.parse(input), nodeClass, *expectedSourceSpans)
        }

        private fun assertSpansFromDocument(
            rootNode: Node,
            nodeClass: KClass<out Node>,
            vararg expectedSourceSpans: SourceSpan,
        ) {
            val node = findNode(rootNode, nodeClass)
            assertEquals(expectedSourceSpans.toList(), node.getSourceSpans())
        }

        private fun findNode(
            rootNode: Node,
            nodeClass: KClass<out Node>,
        ): Node {
            val nodes = ArrayDeque<Node>()
            nodes.addLast(rootNode)
            while (nodes.isNotEmpty()) {
                val node = nodes.removeFirst()
                if (nodeClass.isInstance(node)) {
                    return node
                }
                if (node.firstChild != null) {
                    nodes.addFirst(node.firstChild!!)
                }
                if (node.next != null) {
                    nodes.addLast(node.next!!)
                }
            }
            throw AssertionError("Expected to find $nodeClass node")
        }
    }
}
