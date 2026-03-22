package org.commonmark.test

import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Same as [SpecCoreTest] but converts line endings to Windows-style CR+LF endings before parsing.
 */
class SpecCrLfCoreTest {

    private val parser = Parser.builder().build()
    // The spec says URL-escaping is optional, but the examples assume that it's enabled.
    private val renderer = HtmlRenderer.builder().percentEncodeUrls(true).build()

    private fun render(source: String): String {
        val windowsStyle = source.replace("\n", "\r\n")
        return renderer.render(parser.parse(windowsStyle))
    }

    @Test
    fun testHtmlRendering() {
        val specContent = this::class.java.getResourceAsStream("/spec.txt")
            ?.bufferedReader()
            ?.readText()
            ?: fail("Could not read spec.txt")

        val examples = ExampleReader.readExamples(specContent)
        val failures = mutableListOf<String>()

        for (example in examples) {
            val actual = render(example.source)
            if (actual != example.html) {
                failures.add(
                    "Example ${example.exampleNumber} (${example.section}):\n" +
                            "  Source:   ${example.source.trimEnd().replace("\n", "\\n")}\n" +
                            "  Expected: ${example.html.trimEnd().replace("\n", "\\n")}\n" +
                            "  Actual:   ${actual.trimEnd().replace("\n", "\\n")}"
                )
            }
        }

        if (failures.isNotEmpty()) {
            val total = examples.size
            val passed = total - failures.size
            val message = buildString {
                appendLine("$passed/$total spec examples passed with CR+LF. ${failures.size} failures:")
                appendLine()
                for (failure in failures.take(50)) {
                    appendLine(failure)
                    appendLine()
                }
                if (failures.size > 50) {
                    appendLine("... and ${failures.size - 50} more failures")
                }
            }
            fail(message)
        }
    }
}
