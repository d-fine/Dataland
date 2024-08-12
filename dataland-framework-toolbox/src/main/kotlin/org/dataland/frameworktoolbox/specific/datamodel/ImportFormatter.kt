package org.dataland.frameworktoolbox.specific.datamodel

/**
 * Utility class for formatting imports
 */
object ImportFormatter {
    private const val MAXIMUM_LINE_LENGTH = 120
    private const val FIRST_LINE_START = "import "
    private const val SECOND_LINE_START = "\n    "

    /**
     * Splits a long import statement into multiple lines
     */
    fun splitLongImport(import: String): String {
        if (import.length + FIRST_LINE_START.length <= MAXIMUM_LINE_LENGTH) {
            return FIRST_LINE_START + import
        }
        val splits = import.split(".")
        val builder = StringBuilder()
        builder.append(FIRST_LINE_START)
        var currentLineLength = FIRST_LINE_START.length
        var first = true
        for (split in splits) {
            if (currentLineLength + split.length >= MAXIMUM_LINE_LENGTH) {
                require(split.length + SECOND_LINE_START.length <= MAXIMUM_LINE_LENGTH) {
                    "Import statement is too long to be split"
                }
                builder.append(SECOND_LINE_START)
                currentLineLength = SECOND_LINE_START.length
            }
            if (!first) {
                builder.append(".")
                currentLineLength++
            }
            builder.append(split)
            currentLineLength += split.length
            first = false
        }
        return builder.toString()
    }
}
