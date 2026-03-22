package org.commonmark.ext.footnotes

import org.commonmark.node.*
import org.commonmark.parser.IncludeSourceSpans
import org.commonmark.parser.Parser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FootnotesTest {

    private val extensions = setOf(FootnotesExtension.create())
    private val parser = Parser.builder().extensions(extensions).build()

    @Test
    fun testDefBlockStart() {
        for (s in listOf("1", "a", "^", "*", "\\a", "\uD83D\uDE42", "&0")) {
            val doc = parser.parse("[^$s]: footnote\n")
            val def = find<FootnoteDefinition>(doc)
            assertEquals(s, def.label)
        }

        for (s in listOf("", " ", "a b", "]", "\r", "\n", "\t")) {
            val input = "[^$s]: footnote\n"
            val doc = parser.parse(input)
            assertNull(tryFind<FootnoteDefinition>(doc), "input: $input")
        }
    }

    @Test
    fun testDefBlockStartInterrupts() {
        // This is different from a link reference definition, which can only be at the start of paragraphs.
        val doc = parser.parse("test\n[^1]: footnote\n")
        val paragraph = find<Paragraph>(doc)
        val def = find<FootnoteDefinition>(doc)
        assertEquals("test", (paragraph.lastChild as Text).literal)
        assertEquals("1", def.label)
    }

    @Test
    fun testDefBlockStartIndented() {
        val doc1 = parser.parse("   [^1]: footnote\n")
        assertEquals("1", find<FootnoteDefinition>(doc1).label)
        val doc2 = parser.parse("    [^1]: footnote\n")
        assertNone<FootnoteDefinition>(doc2)
    }

    @Test
    fun testDefMultiple() {
        val doc = parser.parse("[^1]: foo\n[^2]: bar\n")
        val defs = findAll<FootnoteDefinition>(doc)
        assertEquals("1", defs[0].label)
        assertEquals("2", defs[1].label)
    }

    @Test
    fun testDefBlockStartAfterLinkReferenceDefinition() {
        val doc = parser.parse("[foo]: /url\n[^1]: footnote\n")
        val linkReferenceDef = find<LinkReferenceDefinition>(doc)
        val footnotesDef = find<FootnoteDefinition>(doc)
        assertEquals("foo", linkReferenceDef.label)
        assertEquals("1", footnotesDef.label)
    }

    @Test
    fun testDefContainsParagraph() {
        val doc = parser.parse("[^1]: footnote\n")
        val def = find<FootnoteDefinition>(doc)
        val paragraph = def.firstChild as Paragraph
        assertText("footnote", paragraph.firstChild!!)
    }

    @Test
    fun testDefBlockStartSpacesAfterColon() {
        val doc = parser.parse("[^1]:        footnote\n")
        val def = find<FootnoteDefinition>(doc)
        val paragraph = def.firstChild as Paragraph
        assertText("footnote", paragraph.firstChild!!)
    }

    @Test
    fun testDefContainsIndentedCodeBlock() {
        val doc = parser.parse("[^1]:\n        code\n")
        val def = find<FootnoteDefinition>(doc)
        val codeBlock = def.firstChild as IndentedCodeBlock
        assertEquals("code\n", codeBlock.literal)
    }

    @Test
    fun testDefContainsMultipleLines() {
        val doc = parser.parse("[^1]: footnote\nstill\n")
        val def = find<FootnoteDefinition>(doc)
        assertEquals("1", def.label)
        val paragraph = def.firstChild as Paragraph
        assertText("footnote", paragraph.firstChild!!)
        assertText("still", paragraph.lastChild!!)
    }

    @Test
    fun testDefContainsMultipleParagraphs() {
        val doc = parser.parse("[^1]: footnote p1\n\n    footnote p2\n")
        val def = find<FootnoteDefinition>(doc)
        assertEquals("1", def.label)
        val p1 = def.firstChild as Paragraph
        assertText("footnote p1", p1.firstChild!!)
        val p2 = p1.next as Paragraph
        assertText("footnote p2", p2.firstChild!!)
    }

    @Test
    fun testDefFollowedByParagraph() {
        val doc = parser.parse("[^1]: footnote\n\nnormal paragraph\n")
        val def = find<FootnoteDefinition>(doc)
        assertEquals("1", def.label)
        assertText("footnote", def.firstChild!!.firstChild!!)
        assertText("normal paragraph", def.next!!.firstChild!!)
    }

    @Test
    fun testDefContainsList() {
        val doc = parser.parse("[^1]: - foo\n    - bar\n")
        val def = find<FootnoteDefinition>(doc)
        assertEquals("1", def.label)
        val list = def.firstChild as BulletList
        val item1 = list.firstChild as ListItem
        val item2 = list.lastChild as ListItem
        assertText("foo", item1.firstChild!!.firstChild!!)
        assertText("bar", item2.firstChild!!.firstChild!!)
    }

    @Test
    fun testDefInterruptedByOthers() {
        val doc = parser.parse("[^1]: footnote\n# Heading\n")
        val def = find<FootnoteDefinition>(doc)
        val heading = find<Heading>(doc)
        assertEquals("1", def.label)
        assertText("Heading", heading.firstChild!!)
    }

    @Test
    fun testReference() {
        val doc = parser.parse("Test [^foo]\n\n[^foo]: /url\n")
        val ref = find<FootnoteReference>(doc)
        assertEquals("foo", ref.label)
    }

    @Test
    fun testReferenceNoDefinition() {
        val doc = parser.parse("Test [^foo]\n")
        assertNone<FootnoteReference>(doc)
    }

    @Test
    fun testRefWithEmphasisInside() {
        // No emphasis inside footnote reference, should just be treated as text
        val doc = parser.parse("Test [^*foo*]\n\n[^*foo*]: def\n")
        val ref = find<FootnoteReference>(doc)
        assertEquals("*foo*", ref.label)
        assertNull(ref.firstChild)
        val paragraph = doc.firstChild!!
        val text = paragraph.firstChild as Text
        assertEquals("Test ", text.literal)
        assertEquals(ref, text.next)
        assertEquals(ref, paragraph.lastChild)
    }

    @Test
    fun testRefWithEmphasisAround() {
        // Emphasis around footnote reference, the * inside needs to be removed from emphasis processing
        val doc = parser.parse("Test *abc [^foo*] def*\n\n[^foo*]: def\n")
        val ref = find<FootnoteReference>(doc)
        assertEquals("foo*", ref.label)
        assertText("abc ", ref.previous!!)
        assertText(" def", ref.next!!)
        val em = find<Emphasis>(doc)
        assertEquals(em, ref.parent)
    }

    @Test
    fun testRefAfterBang() {
        val doc = parser.parse("Test![^foo]\n\n[^foo]: def\n")
        val ref = find<FootnoteReference>(doc)
        assertEquals("foo", ref.label)
        val paragraph = doc.firstChild!!
        assertText("Test!", paragraph.firstChild!!)
    }

    @Test
    fun testRefAsLabelOnly() {
        // [^bar] is a footnote but [foo] is just text, because full reference links (text `foo`, label `^bar`) don't
        // resolve as footnotes. If `[foo][^bar]` fails to parse as a bracket, `[^bar]` by itself needs to be tried.
        val doc = parser.parse("Test [foo][^bar]\n\n[^bar]: footnote\n")
        val ref = find<FootnoteReference>(doc)
        assertEquals("bar", ref.label)
        val paragraph = doc.firstChild!!
        assertText("Test [foo]", paragraph.firstChild!!)
    }

    @Test
    fun testRefWithEmptyLabel() {
        // [^bar] is a footnote but [] is just text, because collapsed reference links don't resolve as footnotes
        val doc = parser.parse("Test [^bar][]\n\n[^bar]: footnote\n")
        val ref = find<FootnoteReference>(doc)
        assertEquals("bar", ref.label)
        val paragraph = doc.firstChild!!
        assertText("Test ", paragraph.firstChild!!)
        assertText("[]", paragraph.lastChild!!)
    }

    @Test
    fun testRefWithBracket() {
        // Not a footnote, [ needs to be escaped
        val doc = parser.parse("Test [^f[oo]\n\n[^f[oo]: /url\n")
        assertNone<FootnoteReference>(doc)
    }

    @Test
    fun testRefWithBackslash() {
        val doc = parser.parse("[^\\foo]\n\n[^\\foo]: note\n")
        val ref = find<FootnoteReference>(doc)
        assertEquals("\\foo", ref.label)
        val def = find<FootnoteDefinition>(doc)
        assertEquals("\\foo", def.label)
    }

    @Test
    fun testPreferInlineLink() {
        val doc = parser.parse("Test [^bar](/url)\n\n[^bar]: footnote\n")
        assertNone<FootnoteReference>(doc)
    }

    @Test
    fun testPreferReferenceLink() {
        // This is tricky because `[^*foo*][foo]` is a valid link already. If `[foo]` was not defined, the first bracket
        // would be a footnote.
        val doc = parser.parse("Test [^*foo*][foo]\n\n[^*foo*]: /url\n\n[foo]: /url")
        assertNone<FootnoteReference>(doc)
    }

    @Test
    fun testReferenceLinkWithoutDefinition() {
        // Similar to previous test but there's no definition
        val doc = parser.parse("Test [^*foo*][foo]\n\n[^*foo*]: def\n")
        val ref = find<FootnoteReference>(doc)
        assertEquals("*foo*", ref.label)
        val paragraph = doc.firstChild as Paragraph
        assertText("Test ", paragraph.firstChild!!)
        assertText("[foo]", paragraph.lastChild!!)
    }

    @Test
    fun testFootnoteInLink() {
        // Expected to behave the same way as a link within a link, see https://spec.commonmark.org/0.31.2/#example-518
        // i.e. the first (inner) link is parsed, which means the outer one becomes plain text, as nesting links is not
        // allowed.
        val doc = parser.parse("[link with footnote ref [^1]](https://example.com)\n\n[^1]: footnote\n")
        val ref = find<FootnoteReference>(doc)
        assertEquals("1", ref.label)
        val paragraph = doc.firstChild!!
        assertText("[link with footnote ref ", paragraph.firstChild!!)
        assertText("](https://example.com)", paragraph.lastChild!!)
    }

    @Test
    fun testFootnoteWithMarkerInLink() {
        val doc = parser.parse("[link with footnote ref ![^1]](https://example.com)\n\n[^1]: footnote\n")
        val ref = find<FootnoteReference>(doc)
        assertEquals("1", ref.label)
        val paragraph = doc.firstChild!!
        assertText("[link with footnote ref !", paragraph.firstChild!!)
        assertText("](https://example.com)", paragraph.lastChild!!)
    }

    @Test
    fun testInlineFootnote() {
        val extension = FootnotesExtension.builder().inlineFootnotes(true).build()
        val parser = Parser.builder().extensions(setOf(extension)).build()

        run {
            val doc = parser.parse("Test ^[inline footnote]")
            assertText("Test ", doc.firstChild!!.firstChild!!)
            val fn = find<InlineFootnote>(doc)
            assertText("inline footnote", fn.firstChild!!)
        }

        run {
            val doc = parser.parse("Test \\^[not inline footnote]")
            assertNone<InlineFootnote>(doc)
        }

        run {
            val doc = parser.parse("Test ^[not inline footnote")
            assertNone<InlineFootnote>(doc)
            val t = doc.firstChild!!.firstChild!!
            assertText("Test ^[not inline footnote", t)
        }

        run {
            // This is a tricky one because the code span in the link text
            // includes the `]` (and doesn't need to be escaped). Therefore
            // inline footnote parsing has to do full link text parsing/inline parsing.
            // https://spec.commonmark.org/0.31.2/#link-text

            val doc = parser.parse("^[test `bla]`]")
            val fn = find<InlineFootnote>(doc)
            assertText("test ", fn.firstChild!!)
            val code = fn.firstChild!!.next
            assertEquals("bla]", (code as Code).literal)
        }

        run {
            val doc = parser.parse("^[with a [link](url)]")
            val fn = find<InlineFootnote>(doc)
            assertText("with a ", fn.firstChild!!)
            val link = fn.firstChild!!.next
            assertEquals("url", (link as Link).destination)
        }
    }

    @Test
    fun testSourcePositions() {
        val parser = Parser.builder().extensions(extensions)
            .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES).build()

        val doc = parser.parse("Test [^foo]\n\n[^foo]: /url\n")
        val ref = find<FootnoteReference>(doc)
        assertEquals(listOf(SourceSpan.of(0, 5, 5, 6)), ref.getSourceSpans())

        val def = find<FootnoteDefinition>(doc)
        assertEquals(listOf(SourceSpan.of(2, 0, 13, 12)), def.getSourceSpans())
    }

    // Helper methods

    private inline fun <reified T : Node> assertNone(parent: Node) {
        assertNull(tryFind<T>(parent), "Node $parent should not contain ${T::class.simpleName}")
    }

    private inline fun <reified T : Node> find(parent: Node): T {
        return assertNotNull(tryFind<T>(parent), "Could not find a ${T::class.simpleName} node in $parent")
    }

    private inline fun <reified T : Node> tryFind(parent: Node): T? {
        return findAll<T>(parent).firstOrNull()
    }

    private inline fun <reified T : Node> findAll(parent: Node): List<T> {
        val nodes = mutableListOf<T>()
        findAllRecursive(parent, T::class, nodes)
        return nodes
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Node> findAllRecursive(parent: Node, type: kotlin.reflect.KClass<T>, nodes: MutableList<T>) {
        var node = parent.firstChild
        while (node != null) {
            if (type.isInstance(node)) {
                nodes.add(node as T)
            }
            findAllRecursive(node, type, nodes)
            node = node.next
        }
    }

    private fun assertText(expected: String, node: Node) {
        val text = node as Text
        assertEquals(expected, text.literal)
    }
}
