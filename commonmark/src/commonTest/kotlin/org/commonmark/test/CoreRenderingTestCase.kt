package org.commonmark.test

import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import kotlin.test.assertEquals

/**
 * Base class for rendering tests. Provides [render] using the core [Parser] and [HtmlRenderer],
 * and [assertRendering] for comparing rendered output.
 *
 * Subclasses can override [render] to use different parser/renderer configurations.
 *
 * This is a port of the Java `CoreRenderingTestCase` (which extended `RenderingTestCase`).
 * The two classes are merged here since the base class was trivial.
 */
abstract class CoreRenderingTestCase {
    private val parser: Parser = Parser.builder().build()
    private val htmlRenderer: HtmlRenderer = HtmlRenderer.builder().build()

    protected open fun render(source: String): String {
        val node = parser.parse(source)
        return htmlRenderer.render(node)
    }

    protected fun assertRendering(
        source: String,
        expectedResult: String,
    ) {
        val actualResult = render(source)
        // Include source in both expected and actual for better assertion error messages,
        // and replace tabs with a visible arrow character for easier comparison.
        val expected = showTabs("$expectedResult\n\n$source")
        val actual = showTabs("$actualResult\n\n$source")
        assertEquals(expected, actual)
    }

    companion object {
        private fun showTabs(s: String): String {
            // Tabs are shown as "rightwards arrow" for easier comparison
            return s.replace("\t", "\u2192")
        }
    }
}
