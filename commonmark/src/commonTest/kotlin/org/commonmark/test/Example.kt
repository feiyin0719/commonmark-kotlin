package org.commonmark.test

/**
 * A test example from the CommonMark spec.
 */
internal data class Example(
    val filename: String,
    val section: String,
    val info: String,
    val exampleNumber: Int,
    val source: String,
    val html: String,
) {
    override fun toString(): String = "File \"$filename\" section \"$section\" example $exampleNumber"
}
