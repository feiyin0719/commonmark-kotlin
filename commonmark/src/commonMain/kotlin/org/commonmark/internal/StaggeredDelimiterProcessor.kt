package org.commonmark.internal

import org.commonmark.parser.delimiter.DelimiterProcessor
import org.commonmark.parser.delimiter.DelimiterRun

/**
 * An implementation of DelimiterProcessor that dispatches all calls to two or more other DelimiterProcessors
 * depending on the length of the delimiter run. All child DelimiterProcessors must have different minimum
 * lengths. A given delimiter run is dispatched to the child with the largest acceptable minimum length. If no
 * child is applicable, the one with the largest minimum length is chosen.
 */
internal class StaggeredDelimiterProcessor(private val delim: Char) : DelimiterProcessor {

    override val openingCharacter: Char get() = delim
    override val closingCharacter: Char get() = delim
    override var minLength: Int = 0
        private set

    // in reverse minLength order
    private val processors = mutableListOf<DelimiterProcessor>()

    fun add(dp: DelimiterProcessor) {
        val len = dp.minLength
        val it = processors.listIterator()
        var added = false
        while (it.hasNext()) {
            val p = it.next()
            val pLen = p.minLength
            if (len > pLen) {
                it.previous()
                it.add(dp)
                added = true
                break
            } else if (len == pLen) {
                throw IllegalArgumentException(
                    "Cannot add two delimiter processors for char '$delim' and minimum length $len; conflicting processors: $p, $dp"
                )
            }
        }
        if (!added) {
            processors.add(dp)
            this.minLength = len
        }
    }

    private fun findProcessor(len: Int): DelimiterProcessor {
        for (p in processors) {
            if (p.minLength <= len) {
                return p
            }
        }
        return processors.first()
    }

    override fun process(openingRun: DelimiterRun, closingRun: DelimiterRun): Int {
        return findProcessor(openingRun.length).process(openingRun, closingRun)
    }
}
