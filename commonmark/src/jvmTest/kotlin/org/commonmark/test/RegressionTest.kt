package org.commonmark.test

import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Regression tests loaded from resource files.
 * Reads cmark-regression.txt and commonmark.js-regression.txt, parses each example,
 * and checks the rendered HTML output.
 */
class RegressionTest {
    private val parser = Parser.builder().build()

    // The spec says URL-escaping is optional, but the examples assume that it's enabled.
    private val renderer = HtmlRenderer.builder().percentEncodeUrls(true).build()

    private fun render(source: String): String = renderer.render(parser.parse(source))

    private fun assertRendering(
        source: String,
        expectedHtml: String,
    ) {
        val actual = render(source)
        val expected = showTabs("$expectedHtml\n\n$source")
        val actualFormatted = showTabs("$actual\n\n$source")
        assertEquals(expected, actualFormatted)
    }

    @Test
    fun testRegressionExamples() {
        val regressionResources = listOf("/cmark-regression.txt", "/commonmark.js-regression.txt")
        val allExamples = mutableListOf<Example>()
        val missingResources = mutableListOf<String>()

        for (resourcePath in regressionResources) {
            val content =
                this::class.java
                    .getResourceAsStream(resourcePath)
                    ?.bufferedReader()
                    ?.readText()

            if (content == null) {
                missingResources.add(resourcePath)
                continue
            }

            allExamples.addAll(ExampleReader.readExamples(content, resourcePath))
        }

        if (allExamples.isEmpty()) {
            fail("No regression test examples found. Missing resources: $missingResources")
        }

        val overriddenExamples = getOverriddenExamples()
        val failures = mutableListOf<String>()

        for (example in allExamples) {
            val expectedHtml = overriddenExamples[example.source] ?: example.html
            val actual = render(example.source)
            if (actual != expectedHtml) {
                failures.add(
                    "Example ${example.exampleNumber} (${example.section}):\n" +
                        "  Source:   ${example.source.trimEnd().replace("\n", "\\n")}\n" +
                        "  Expected: ${expectedHtml.trimEnd().replace("\n", "\\n")}\n" +
                        "  Actual:   ${actual.trimEnd().replace("\n", "\\n")}",
                )
            }
        }

        if (failures.isNotEmpty()) {
            val total = allExamples.size
            val passed = total - failures.size
            val message =
                buildString {
                    appendLine("$passed/$total regression examples passed. ${failures.size} failures:")
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

    companion object {
        private fun showTabs(s: String): String = s.replace("\t", "\u2192")

        private fun getOverriddenExamples(): Map<String, String> {
            val m = mutableMapOf<String, String>()

            // The only difference is that we don't change %28 and %29 to ( and ) (percent encoding is preserved)
            m["[XSS](javascript&amp;colon;alert%28&#039;XSS&#039;%29)\n"] =
                "<p><a href=\"javascript&amp;colon;alert%28'XSS'%29\">XSS</a></p>\n"
            // Callers should handle BOMs
            m["\uFEFF# Hi\n"] = "<p>\uFEFF# Hi</p>\n"

            return m
        }
    }
}
