package org.dataland.csvconverter.csv.utils

class EnumCsvParser<T>(val valueMap: Map<String, T>) {
    fun parse(columnReference: String, input: String?): T {
        if (input == null)
            throw createException("null", columnReference)

        return valueMap[input] ?: throw createException(input, columnReference)
    }

    fun parseAllowingNull(columnReference: String, input: String?): T? {
        return input?.let { parse(columnReference, it) }
    }

    private fun createException(illegalInput: String, columnReference: String?): IllegalArgumentException {
        return IllegalArgumentException("Received illegal value $illegalInput for column: $columnReference. Allowed values are ${valueMap.keys}")
    }
}
