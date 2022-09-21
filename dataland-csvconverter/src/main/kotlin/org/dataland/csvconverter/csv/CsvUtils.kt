package org.dataland.csvconverter.csv

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import java.io.FileReader
import java.math.BigDecimal
import java.nio.charset.StandardCharsets

/**
 * This object provides utility functions for parsing data from CSV files in the Dataland context
 */
object CsvUtils {

    const val NOT_AVAILABLE_STRING = "n/a"
    var EURO_UNIT_CONVERSION_FACTOR = "1000000"

    /**
     * This function parses a CSV file given a filename
     */
    inline fun <reified T> readCsvFile(fileName: String): List<T> {
        FileReader(fileName, StandardCharsets.UTF_8).use {
            return CsvMapper()
                .readerFor(T::class.java)
                .with(CsvSchema.emptySchema().withHeader().withColumnSeparator(';'))
                .readValues<T>(it)
                .readAll()
                .toList()
        }
    }

    /**
     * This function uses the backing map to extract a property from a CSV row.
     * If the requested column is not populated "n/a" will be returned
     */
    fun Map<String, String>.getCsvValue(property: String, csvData: Map<String, String>): String? {
        return csvData[this[property]!!]?.trim()?.ifBlank {
            null
        }
    }

    /** This function checks, whether the passed properties field contains an entry.
     * If it does, it returns true
     */
    fun Map<String, String>.checkIfFieldHasValue(property: String, csvData: Map<String, String>): Boolean {
        return csvData[this[property]!!]!!.isNotBlank()
    }

    /**
     * This function uses the backing map to extract a numeric property from a CSV row.
     * The returned value is scaled according to the scaleFactor
     */
    fun Map<String, String>.getScaledCsvValue(
        property: String,
        csvData: Map<String, String>,
        scaleFactor: String
    ): BigDecimal? {
        // The numeric value conversion assumes "," as decimal separator and "." to separate thousands
        return getCsvValue(property, csvData)?.replace("[^,\\d]".toRegex(), "")?.replace(",", ".")
            ?.toBigDecimalOrNull()?.multiply(scaleFactor.toBigDecimal())?.stripTrailingZeros()
    }

    /**
     * This function uses the backing map to extract a numeric property from a CSV row.
     * If the value represents a percentage, it gets scaled accordingly. All other values get scaled using the
     * EURO_UNIT_CONVERSION_FACTOR
     */
    fun Map<String, String>.getNumericCsvValue(property: String, csvLineData: Map<String, String>): BigDecimal? {
        return if (getCsvValue(property, csvLineData)?.contains("%") == true) {
            getScaledCsvValue(property, csvLineData, "0.01")
        } else {
            getScaledCsvValue(property, csvLineData, EURO_UNIT_CONVERSION_FACTOR)
        }
    }
}
