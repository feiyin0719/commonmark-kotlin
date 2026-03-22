package org.commonmark.internal

internal class BlockContent {

    private val sb: StringBuilder
    private var lineCount: Int = 0

    constructor() {
        sb = StringBuilder()
    }

    constructor(content: String) {
        sb = StringBuilder(content)
    }

    fun add(line: CharSequence) {
        if (lineCount != 0) {
            sb.append('\n')
        }
        sb.append(line)
        lineCount++
    }

    fun getString(): String = sb.toString()
}
