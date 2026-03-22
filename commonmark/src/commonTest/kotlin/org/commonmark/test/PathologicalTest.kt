package org.commonmark.test

import kotlin.test.Test

/**
 * Pathological input cases (from commonmark.js).
 */
class PathologicalTest : CoreRenderingTestCase() {
    private var x = 100_000

    @Test
    fun nestedStrongEmphasis() {
        // this is limited by the stack size because visitor is recursive
        x = 500
        assertRendering(
            "*a **a ".repeat(x) + "b" + " a** a*".repeat(x),
            "<p>" + "<em>a <strong>a ".repeat(x) + "b" +
                " a</strong> a</em>".repeat(x) + "</p>\n",
        )
    }

    @Test
    fun emphasisClosersWithNoOpeners() {
        assertRendering(
            "a_ ".repeat(x),
            "<p>" + "a_ ".repeat(x - 1) + "a_</p>\n",
        )
    }

    @Test
    fun emphasisOpenersWithNoClosers() {
        assertRendering(
            "_a ".repeat(x),
            "<p>" + "_a ".repeat(x - 1) + "_a</p>\n",
        )
    }

    @Test
    fun linkClosersWithNoOpeners() {
        assertRendering(
            "a] ".repeat(x),
            "<p>" + "a] ".repeat(x - 1) + "a]</p>\n",
        )
    }

    @Test
    fun linkOpenersWithNoClosers() {
        assertRendering(
            "[a ".repeat(x),
            "<p>" + "[a ".repeat(x - 1) + "[a</p>\n",
        )
    }

    @Test
    fun linkOpenersAndEmphasisClosers() {
        assertRendering(
            "[ a_ ".repeat(x),
            "<p>" + "[ a_ ".repeat(x - 1) + "[ a_</p>\n",
        )
    }

    @Test
    fun mismatchedOpenersAndClosers() {
        assertRendering(
            "*a_ ".repeat(x),
            "<p>" + "*a_ ".repeat(x - 1) + "*a_</p>\n",
        )
    }

    @Test
    fun nestedBrackets() {
        assertRendering(
            "[".repeat(x) + "a" + "]".repeat(x),
            "<p>" + "[".repeat(x) + "a" + "]".repeat(x) + "</p>\n",
        )
    }

    @Test
    fun nestedBlockQuotes() {
        // this is limited by the stack size because visitor is recursive
        x = 1000
        assertRendering(
            "> ".repeat(x) + "a\n",
            "<blockquote>\n".repeat(x) + "<p>a</p>\n" +
                "</blockquote>\n".repeat(x),
        )
    }

    @Test
    fun hugeHorizontalRule() {
        assertRendering(
            "*".repeat(10000) + "\n",
            "<hr />\n",
        )
    }

    @Test
    fun backslashInLink() {
        // See https://github.com/commonmark/commonmark.js/issues/157
        assertRendering(
            "[" + "\\".repeat(x) + "\n",
            "<p>" + "[" + "\\".repeat(x / 2) + "</p>\n",
        )
    }

    @Test
    fun unclosedInlineLinks() {
        // See https://github.com/commonmark/commonmark.js/issues/129
        assertRendering(
            "[](".repeat(x) + "\n",
            "<p>" + "[](".repeat(x) + "</p>\n",
        )
    }
}
