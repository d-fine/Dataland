package org.dataland.csvconverter.csv

/**
 * This interface is used to parse framework data from a CSV file
 */
interface CsvFrameworkParser<T> {
    /**
     * This function returns, whether a specific line is suitable for parsing
     * with this parser
     */
    fun validateLine(row: Map<String, String>): Boolean

    /**
     * This function actually parses extracts the data from the CSV Row
     */
    fun buildData(row: Map<String, String>): T
}
