package org.commonmark.internal

import org.commonmark.internal.util.Escaping
import org.commonmark.internal.util.LinkScanner
import org.commonmark.node.LinkReferenceDefinition
import org.commonmark.node.SourceSpan
import org.commonmark.parser.SourceLine
import org.commonmark.parser.SourceLines
import org.commonmark.parser.beta.Scanner

/**
 * Parser for link reference definitions at the beginning of a paragraph.
 *
 * @see [Link reference definitions](https://spec.commonmark.org/0.31.2/#link-reference-definitions)
 */
internal class LinkReferenceDefinitionParser {

    private var state = State.START_DEFINITION

    private val _paragraphLines = mutableListOf<SourceLine>()
    private val _definitions = mutableListOf<LinkReferenceDefinition>()
    private val _sourceSpans = mutableListOf<SourceSpan>()

    private var label: StringBuilder? = null
    private var destination: String? = null
    private var titleDelimiter: Char = '\u0000'
    private var title: StringBuilder? = null
    private var referenceValid = false

    fun parse(line: SourceLine) {
        _paragraphLines.add(line)
        if (state == State.PARAGRAPH) {
            // We're in a paragraph now. Link reference definitions can only appear at the beginning, so once
            // we're in a paragraph, there's no going back.
            return
        }

        val scanner = Scanner.of(SourceLines.of(line))
        while (scanner.hasNext()) {
            val success = when (state) {
                State.START_DEFINITION -> startDefinition(scanner)
                State.LABEL -> label(scanner)
                State.DESTINATION -> destination(scanner)
                State.START_TITLE -> startTitle(scanner)
                State.TITLE -> title(scanner)
                else -> throw IllegalStateException("Unknown parsing state: $state")
            }
            // Parsing failed, which means we fall back to treating text as a paragraph.
            if (!success) {
                state = State.PARAGRAPH
                // If parsing of the title part failed, we still have a valid reference that we can add, and we need to
                // do it before the source span for this line is added.
                finishReference()
                return
            }
        }
    }

    fun addSourceSpan(sourceSpan: SourceSpan) {
        _sourceSpans.add(sourceSpan)
    }

    /**
     * @return the lines that are normal paragraph content, without newlines
     */
    val paragraphLines: SourceLines
        get() = SourceLines.of(_paragraphLines)

    val paragraphSourceSpans: List<SourceSpan>
        get() = _sourceSpans

    fun getDefinitions(): List<LinkReferenceDefinition> {
        finishReference()
        return _definitions
    }

    val currentState: State
        get() = state

    fun removeLines(lines: Int): List<SourceSpan> {
        val removedSpans = ArrayList(
            _sourceSpans.subList(maxOf(_sourceSpans.size - lines, 0), _sourceSpans.size)
        ).toList()
        removeLast(lines, _paragraphLines)
        removeLast(lines, _sourceSpans)
        return removedSpans
    }

    private fun startDefinition(scanner: Scanner): Boolean {
        // Finish any outstanding references now. We don't do this earlier because we need addSourceSpan to have been
        // called before we do it.
        finishReference()

        scanner.whitespace()
        if (!scanner.next('[')) {
            return false
        }

        state = State.LABEL
        label = StringBuilder()

        if (!scanner.hasNext()) {
            label!!.append('\n')
        }
        return true
    }

    private fun label(scanner: Scanner): Boolean {
        val start = scanner.position()
        if (!LinkScanner.scanLinkLabelContent(scanner)) {
            return false
        }

        label!!.append(scanner.getSource(start, scanner.position()).getContent())

        if (!scanner.hasNext()) {
            // label might continue on next line
            label!!.append('\n')
            return true
        } else if (scanner.next(']')) {
            // end of label
            if (!scanner.next(':')) {
                return false
            }

            // spec: A link label can have at most 999 characters inside the square brackets.
            if (label!!.length > 999) {
                return false
            }

            val normalizedLabel = Escaping.normalizeLabelContent(label!!.toString())
            if (normalizedLabel.isEmpty()) {
                return false
            }

            state = State.DESTINATION

            scanner.whitespace()
            return true
        } else {
            return false
        }
    }

    private fun destination(scanner: Scanner): Boolean {
        scanner.whitespace()
        val start = scanner.position()
        if (!LinkScanner.scanLinkDestination(scanner)) {
            return false
        }

        val rawDestination = scanner.getSource(start, scanner.position()).getContent()
        destination = if (rawDestination.startsWith("<")) {
            rawDestination.substring(1, rawDestination.length - 1)
        } else {
            rawDestination
        }

        val whitespace = scanner.whitespace()
        if (!scanner.hasNext()) {
            // Destination was at end of line, so this is a valid reference for sure (and maybe a title).
            // If not at end of line, wait for title to be valid first.
            referenceValid = true
            _paragraphLines.clear()
        } else if (whitespace == 0) {
            // spec: The title must be separated from the link destination by whitespace
            return false
        }

        state = State.START_TITLE
        return true
    }

    private fun startTitle(scanner: Scanner): Boolean {
        scanner.whitespace()
        if (!scanner.hasNext()) {
            state = State.START_DEFINITION
            return true
        }

        titleDelimiter = '\u0000'
        val c = scanner.peek()
        when (c) {
            '"', '\'' -> titleDelimiter = c
            '(' -> titleDelimiter = ')'
        }

        if (titleDelimiter != '\u0000') {
            state = State.TITLE
            title = StringBuilder()
            scanner.next()
            if (!scanner.hasNext()) {
                title!!.append('\n')
            }
        } else {
            // There might be another reference instead, try that for the same character.
            state = State.START_DEFINITION
        }
        return true
    }

    private fun title(scanner: Scanner): Boolean {
        val start = scanner.position()
        if (!LinkScanner.scanLinkTitleContent(scanner, titleDelimiter)) {
            // Invalid title, stop. Title collected so far must not be used.
            title = null
            return false
        }

        title!!.append(scanner.getSource(start, scanner.position()).getContent())

        if (!scanner.hasNext()) {
            // Title ran until the end of line, so continue on next line (until we find the delimiter)
            title!!.append('\n')
            return true
        }

        // Skip delimiter character
        scanner.next()
        scanner.whitespace()
        if (scanner.hasNext()) {
            // spec: No further non-whitespace characters may occur on the line.
            // Title collected so far must not be used.
            title = null
            return false
        }
        referenceValid = true
        _paragraphLines.clear()

        // See if there's another definition.
        state = State.START_DEFINITION
        return true
    }

    private fun finishReference() {
        if (!referenceValid) {
            return
        }

        val d = Escaping.unescapeString(destination!!)
        val t = if (title != null) Escaping.unescapeString(title!!.toString()) else null
        val definition = LinkReferenceDefinition(label!!.toString(), d, t)
        definition.setSourceSpans(_sourceSpans.toList())
        _sourceSpans.clear()
        _definitions.add(definition)

        label = null
        referenceValid = false
        destination = null
        title = null
    }

    internal enum class State {
        // Looking for the start of a definition, i.e. `[`
        START_DEFINITION,
        // Parsing the label, i.e. `foo` within `[foo]`
        LABEL,
        // Parsing the destination, i.e. `/url` in `[foo]: /url`
        DESTINATION,
        // Looking for the start of a title, i.e. the first `"` in `[foo]: /url "title"`
        START_TITLE,
        // Parsing the content of the title, i.e. `title` in `[foo]: /url "title"`
        TITLE,

        // End state, no matter what kind of lines we add, they won't be references
        PARAGRAPH
    }

    companion object {
        private fun <T> removeLast(n: Int, list: MutableList<T>) {
            if (n >= list.size) {
                list.clear()
            } else {
                for (i in 0 until n) {
                    list.removeAt(list.size - 1)
                }
            }
        }
    }
}
