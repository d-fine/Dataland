package org.dataland.csvconverter.csv.utils

/**
 * This class realises a generic Enum-Parser
 * that maps a set of predefined string values to internal object references (e.g Enums)
 */
open class EnumCsvParser<T>(private val valueMap: Map<String, T>) {
    /**
     * Parses the input string input. If input is null or no mapping is defined for it, an error is raised
     * @param columnReference A column reference name that is solely used to generate sensible error messages!
     * @param input The input that is supposed to be parsed
     */
    fun parse(columnReference: String, input: String?): T {
        if (input == null) {
            throw createException("null", columnReference)
        }

        return valueMap[input] ?: throw createException(input, columnReference)
    }

    /**
     * Parses the input string input. If no mapping is defined for the input, an error is raised
     * @param columnReference A column reference name that is solely used to generate sensible error messages!
     * @param input The input that is supposed to be parsed
     */
    fun parseAllowingNull(columnReference: String, input: String?): T? {
        return input?.let { parse(columnReference, it) }
    }

    private fun createException(illegalInput: String, columnReference: String?): IllegalArgumentException {
        return IllegalArgumentException(
            "Received illegal value $illegalInput for column: $columnReference." +
                " Allowed values are ${valueMap.keys}",
        )
    }
}
