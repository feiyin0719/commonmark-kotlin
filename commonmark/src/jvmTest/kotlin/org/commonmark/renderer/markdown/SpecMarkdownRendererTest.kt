package org.commonmark.renderer.markdown

import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.test.ExampleReader
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Tests Markdown rendering using the examples in the spec like this:
 *
 * 1. Parses the source to an AST and then renders it back to Markdown
 * 2. Parses that to an AST and then renders it to HTML
 * 3. Compares that HTML to the expected HTML of the example:
 *    If it's the same, then the expected elements were preserved in the Markdown rendering
 */
class SpecMarkdownRendererTest {

    @Test
    fun testCoverage() {
        val specContent = this::class.java.getResourceAsStream("/spec.txt")
            ?.bufferedReader()
            ?.readText()
            ?: fail("Could not read spec.txt")

        val examples = ExampleReader.readExamples(specContent)
        val passes = mutableListOf<org.commonmark.test.Example>()
        val fails = mutableListOf<org.commonmark.test.Example>()
        for (example in examples) {
            val markdown = renderMarkdown(example.source)
            val rendered = renderHtml(markdown)
            if (rendered == example.html.replace("\t", "\u2192")) {
                passes.add(example)
            } else {
                fails.add(example)
            }
        }

        println("Passed examples by section (total ${passes.size}):")
        printCountsBySection(passes)
        println()

        println("Failed examples by section (total ${fails.size}):")
        printCountsBySection(fails)
        println()

        println("Failed examples:")
        for (fail in fails) {
            println("Failed: $fail")
            println("````````````````````````````````")
            print(fail.source)
            println("````````````````````````````````")
            println()
        }

        assertTrue(passes.size >= 652, "Expected at least 652 passing examples but got ${passes.size}")
        assertTrue(fails.isEmpty(), "Expected no failing examples but got ${fails.size}")
    }

    private fun printCountsBySection(examples: List<org.commonmark.test.Example>) {
        val bySection = linkedMapOf<String, Int>()
        for (example in examples) {
            val count = bySection[example.section] ?: 0
            bySection[example.section] = count + 1
        }
        for ((section, count) in bySection) {
            println("$count: $section")
        }
    }

    private fun parse(source: String): Node {
        return Parser.builder().build().parse(source)
    }

    private fun renderMarkdown(source: String): String {
        return MARKDOWN_RENDERER.render(parse(source))
    }

    private fun renderHtml(source: String): String {
        // The spec uses "rightwards arrow" to show tabs
        return HTML_RENDERER.render(parse(source)).replace("\t", "\u2192")
    }

    companion object {
        val MARKDOWN_RENDERER: MarkdownRenderer = MarkdownRenderer.builder().build()
        // The spec says URL-escaping is optional, but the examples assume that it's enabled.
        val HTML_RENDERER: HtmlRenderer = HtmlRenderer.builder().percentEncodeUrls(true).build()
    }
}
