package org.commonmark.internal

import org.commonmark.internal.LinkReferenceDefinitionParser.State
import org.commonmark.node.LinkReferenceDefinition
import org.commonmark.parser.SourceLine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LinkReferenceDefinitionParserTest {

    private val parser = LinkReferenceDefinitionParser()

    @Test
    fun testStartLabel() {
        assertState("[", State.LABEL, "[")
    }

    @Test
    fun testStartNoLabel() {
        // Not a label
        assertParagraph("a")
        // Can not go back to parsing link reference definitions
        parse("a")
        parse("[")
        assertEquals(State.PARAGRAPH, parser.currentState)
        assertParagraphLines("a\n[", parser)
    }

    @Test
    fun testEmptyLabel() {
        assertParagraph("[]: /")
        assertParagraph("[ ]: /")
        assertParagraph("[ \t\n\u000B\u000C\r ]: /")
    }

    @Test
    fun testLabelColon() {
        assertParagraph("[foo] : /")
    }

    @Test
    fun testLabel() {
        assertState("[foo]:", State.DESTINATION, "[foo]:")
        assertState("[ foo ]:", State.DESTINATION, "[ foo ]:")
    }

    @Test
    fun testLabelInvalid() {
        assertParagraph("[foo[]:")
    }

    @Test
    fun testLabelMultiline() {
        parse("[two")
        assertEquals(State.LABEL, parser.currentState)
        parse("lines]:")
        assertEquals(State.DESTINATION, parser.currentState)
        parse("/url")
        assertEquals(State.START_TITLE, parser.currentState)
        assertDef(parser.getDefinitions()[0], "two\nlines", "/url", null)
    }

    @Test
    fun testLabelStartsWithNewline() {
        parse("[")
        assertEquals(State.LABEL, parser.currentState)
        parse("weird]:")
        assertEquals(State.DESTINATION, parser.currentState)
        parse("/url")
        assertEquals(State.START_TITLE, parser.currentState)
        assertDef(parser.getDefinitions()[0], "\nweird", "/url", null)
    }

    @Test
    fun testDestination() {
        parse("[foo]: /url")
        assertEquals(State.START_TITLE, parser.currentState)
        assertParagraphLines("", parser)

        assertEquals(1, parser.getDefinitions().size)
        assertDef(parser.getDefinitions()[0], "foo", "/url", null)

        parse("[bar]: </url2>")
        assertDef(parser.getDefinitions()[1], "bar", "/url2", null)
    }

    @Test
    fun testDestinationInvalid() {
        assertParagraph("[foo]: <bar<>")
    }

    @Test
    fun testTitle() {
        parse("[foo]: /url 'title'")
        assertEquals(State.START_DEFINITION, parser.currentState)
        assertParagraphLines("", parser)

        assertEquals(1, parser.getDefinitions().size)
        assertDef(parser.getDefinitions()[0], "foo", "/url", "title")
    }

    @Test
    fun testTitleStartWhitespace() {
        parse("[foo]: /url")
        assertEquals(State.START_TITLE, parser.currentState)
        assertParagraphLines("", parser)

        parse("   ")

        assertEquals(State.START_DEFINITION, parser.currentState)
        assertParagraphLines("   ", parser)

        assertEquals(1, parser.getDefinitions().size)
        assertDef(parser.getDefinitions()[0], "foo", "/url", null)
    }

    @Test
    fun testTitleMultiline() {
        parse("[foo]: /url 'two")
        assertEquals(State.TITLE, parser.currentState)
        assertParagraphLines("[foo]: /url 'two", parser)
        assertTrue(parser.getDefinitions().isEmpty())

        parse("lines")
        assertEquals(State.TITLE, parser.currentState)
        assertParagraphLines("[foo]: /url 'two\nlines", parser)
        assertTrue(parser.getDefinitions().isEmpty())

        parse("'")
        assertEquals(State.START_DEFINITION, parser.currentState)
        assertParagraphLines("", parser)

        assertEquals(1, parser.getDefinitions().size)
        assertDef(parser.getDefinitions()[0], "foo", "/url", "two\nlines\n")
    }

    @Test
    fun testTitleMultiline2() {
        parse("[foo]: /url '")
        assertEquals(State.TITLE, parser.currentState)
        parse("title'")
        assertEquals(State.START_DEFINITION, parser.currentState)

        assertDef(parser.getDefinitions()[0], "foo", "/url", "\ntitle")
    }

    @Test
    fun testTitleMultiline3() {
        parse("[foo]: /url")
        assertEquals(State.START_TITLE, parser.currentState)
        // Note that this looks like a valid title until we parse "bad", at which point we need to treat the whole line
        // as a paragraph line and discard any already parsed title.
        parse("\"title\" bad")
        assertEquals(State.PARAGRAPH, parser.currentState)

        assertDef(parser.getDefinitions()[0], "foo", "/url", null)
    }

    @Test
    fun testTitleMultiline4() {
        parse("[foo]: /url")
        assertEquals(State.START_TITLE, parser.currentState)
        parse("(title")
        assertEquals(State.TITLE, parser.currentState)
        parse("foo(")
        assertEquals(State.PARAGRAPH, parser.currentState)

        assertDef(parser.getDefinitions()[0], "foo", "/url", null)
    }

    @Test
    fun testTitleInvalid() {
        assertParagraph("[foo]: /url (invalid(")
        assertParagraph("[foo]: </url>'title'")
        assertParagraph("[foo]: /url 'title' INVALID")
    }

    private fun parse(content: String) {
        parser.parse(SourceLine.of(content, null))
    }

    companion object {
        private fun assertParagraph(input: String) {
            assertState(input, State.PARAGRAPH, input)
        }

        private fun assertState(input: String, state: State, paragraphContent: String) {
            val parser = LinkReferenceDefinitionParser()
            // TODO: Should we check things with source spans here?
            parser.parse(SourceLine.of(input, null))
            assertEquals(state, parser.currentState)
            assertParagraphLines(paragraphContent, parser)
        }

        private fun assertDef(def: LinkReferenceDefinition, label: String, destination: String, title: String?) {
            assertEquals(label, def.label)
            assertEquals(destination, def.destination)
            assertEquals(title, def.title)
        }

        private fun assertParagraphLines(expectedContent: String, parser: LinkReferenceDefinitionParser) {
            val actual = parser.paragraphLines.getContent()
            assertEquals(expectedContent, actual)
        }
    }
}
