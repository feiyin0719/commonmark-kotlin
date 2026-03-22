package org.commonmark.test

/**
 * Reader for files containing examples of CommonMark source and expected HTML rendering.
 */
internal object ExampleReader {

    private val SECTION_PATTERN = Regex("#{1,6} *(.*)")
    private const val EXAMPLE_START_MARKER = "```````````````````````````````` example"
    private const val EXAMPLE_END_MARKER = "````````````````````````````````"

    fun readExamples(content: String, filename: String = "spec.txt"): List<Example> {
        val examples = mutableListOf<Example>()
        var state = State.BEFORE
        var section = ""
        var info = ""
        var source = StringBuilder()
        var html = StringBuilder()
        var exampleNumber = 0

        for (line in content.lines()) {
            when (state) {
                State.BEFORE -> {
                    val matcher = SECTION_PATTERN.matchEntire(line)
                    if (matcher != null) {
                        section = matcher.groupValues[1]
                        exampleNumber = 0
                    }
                    if (line.startsWith(EXAMPLE_START_MARKER)) {
                        info = line.substring(EXAMPLE_START_MARKER.length).trim()
                        state = State.SOURCE
                        exampleNumber++
                    }
                }
                State.SOURCE -> {
                    if (line == ".") {
                        state = State.HTML
                    } else {
                        val processedLine = line.replace('\u2192', '\t')
                        source.append(processedLine).append('\n')
                    }
                }
                State.HTML -> {
                    if (line == EXAMPLE_END_MARKER) {
                        state = State.BEFORE
                        examples.add(
                            Example(filename, section, info, exampleNumber,
                                source.toString(), html.toString())
                        )
                        source = StringBuilder()
                        html = StringBuilder()
                    } else {
                        val processedLine = line.replace('\u2192', '\t')
                        html.append(processedLine).append('\n')
                    }
                }
            }
        }
        return examples
    }

    private enum class State {
        BEFORE, SOURCE, HTML
    }
}
