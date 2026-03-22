package org.commonmark.test

import kotlin.test.Test

class HeadingParserTest : CoreRenderingTestCase() {

    @Test
    fun atxHeadingStart() {
        assertRendering("# test", "<h1>test</h1>\n")
        assertRendering("###### test", "<h6>test</h6>\n")
        assertRendering("####### test", "<p>####### test</p>\n")
        assertRendering("#test", "<p>#test</p>\n")
        assertRendering("#", "<h1></h1>\n")
    }

    @Test
    fun atxHeadingTrailing() {
        assertRendering("# test #", "<h1>test</h1>\n")
        assertRendering("# test ###", "<h1>test</h1>\n")
        assertRendering("# test # ", "<h1>test</h1>\n")
        assertRendering("# test  ###  ", "<h1>test</h1>\n")
        assertRendering("# test # #", "<h1>test #</h1>\n")
        assertRendering("# test#", "<h1>test#</h1>\n")
    }

    @Test
    fun atxHeadingSurrogates() {
        assertRendering("# \uD83D\uDE0A #", "<h1>\uD83D\uDE0A</h1>\n")
    }

    @Test
    fun setextHeadingMarkers() {
        assertRendering("test\n=", "<h1>test</h1>\n")
        assertRendering("test\n-", "<h2>test</h2>\n")
        assertRendering("test\n====", "<h1>test</h1>\n")
        assertRendering("test\n----", "<h2>test</h2>\n")
        assertRendering("test\n====   ", "<h1>test</h1>\n")
        assertRendering("test\n====   =", "<p>test\n====   =</p>\n")
        assertRendering("test\n=-=", "<p>test\n=-=</p>\n")
        assertRendering("test\n=a", "<p>test\n=a</p>\n")
    }
}
