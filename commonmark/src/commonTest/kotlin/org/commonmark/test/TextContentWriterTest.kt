package org.commonmark.test

import org.commonmark.renderer.text.TextContentWriter
import kotlin.test.Test
import kotlin.test.assertEquals

class TextContentWriterTest {

    @Test
    fun whitespace() {
        val sb = StringBuilder()
        val writer = TextContentWriter(sb)
        writer.write("foo")
        writer.whitespace()
        writer.write("bar")
        assertEquals("foo bar", sb.toString())
    }

    @Test
    fun colon() {
        val sb = StringBuilder()
        val writer = TextContentWriter(sb)
        writer.write("foo")
        writer.colon()
        writer.write("bar")
        assertEquals("foo:bar", sb.toString())
    }

    @Test
    fun line() {
        val sb = StringBuilder()
        val writer = TextContentWriter(sb)
        writer.write("foo")
        writer.line()
        writer.write("bar")
        assertEquals("foo\nbar", sb.toString())
    }

    @Test
    fun writeStripped() {
        val sb = StringBuilder()
        val writer = TextContentWriter(sb)
        writer.writeStripped("foo\n bar")
        assertEquals("foo bar", sb.toString())
    }

    @Test
    fun write() {
        val sb = StringBuilder()
        val writer = TextContentWriter(sb)
        writer.writeStripped("foo bar")
        assertEquals("foo bar", sb.toString())
    }
}
