package org.dataland.csvconverter.csv

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import java.io.FileReader
import java.math.BigDecimal
import java.nio.charset.StandardCharsets

/**
 * This object provides utility functions for parsing data from CSV files in the Dataland context
 */
object CsvUtils {
    val SCALE_FACTOR_ONE_MILLION = "1000000".toBigDecimal()

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
     * If the requested column is not populated null will be returned
     */
    fun Map<String, String>.getCsvValueAllowingNull(property: String, csvData: Map<String, String>): String? {
        return csvData[this[property]!!.lowercase()]?.trim()?.ifBlank {
            null
        }
    }

    /**
     * This function uses the backing map to extract a property from a CSV row, where the field itself represents a
     * list of items separated by commas. If the requested column is not populated an empty list will
     * be returned.
     */
    fun Map<String, String>.readMultiValuedCsvField(
        property: String,
        csvData: Map<String, String>,
    ): List<String> {
        val fieldValue = getCsvValueAllowingNull(property, csvData)
        val csvMapper = CsvMapper().apply { enable(CsvParser.Feature.TRIM_SPACES) }
        return if (fieldValue == null) {
            emptyList()
        } else {
            csvMapper.readerFor(String::class.java)
                .with(CsvSchema.emptySchema().withoutHeader())
                .readValues<String>(fieldValue).readAll()
        }
    }

    /**
     * This function uses the backing map to extract a property from a CSV row
     * If the requested column is not populated an exception will be raised
     */
    fun Map<String, String>.getCsvValue(property: String, csvData: Map<String, String>): String {
        return this.getCsvValueAllowingNull(property, csvData)
            ?: throw IllegalArgumentException("The column ${this[property]} is empty but shouldn't be")
    }

    /**
     * This function checks, whether the passed properties field contains an entry.
     * If it does, it returns true
     */
    private fun Map<String, String>.checkIfFieldHasValue(property: String, csvData: Map<String, String>): Boolean {
        return csvData[this[property]!!.lowercase()]!!.isNotBlank()
    }

    /**
     * Checks if any of the specified fields have a value
     */
    fun Map<String, String>.checkIfAnyFieldHasValue(fields: List<String>, csvData: Map<String, String>): Boolean {
        return fields.any { this.checkIfFieldHasValue(it, csvData) }
    }

    /**
     * Tries to parse a percentage value in the format "XX,XX %" with a comma separator
     */
    fun Map<String, String>.readCsvPercentage(property: String, csvData: Map<String, String>): BigDecimal? {
        val rawValue = this.getCsvValueAllowingNull(property, csvData)?.trim() ?: return null
        val expectedFormat = "\\d+(,\\d+)?(\\s*)%".toRegex()
        if (!rawValue.matches(expectedFormat)) {
            throw IllegalArgumentException(
                "The input string \"$rawValue\" for column ${this[property]} does not " +
                    "match the expected format for a percentage value",
            )
        }

        return rawValue
            .replace("[%\\s]".toRegex(), "")
            .replace(",", ".")
            .toBigDecimal()
            .multiply("0.01".toBigDecimal())
            .stripTrailingZeros()
    }

    /**
     * Tries to parse a decimal value from the CSV file with the expected format XXX.XXX,XXX allowing null
     */
    fun Map<String, String>.readCsvDecimal(
        property: String,
        csvData: Map<String, String>,
        scaleFactor: BigDecimal = BigDecimal.ONE,
    ): BigDecimal? {
        val rawValue = this.getCsvValueAllowingNull(property, csvData)?.trim() ?: return null
        val expectedFormat = "(\\d+(\\.)?)+(,\\d+)?".toRegex()
        if (!rawValue.matches(expectedFormat)) {
            throw IllegalArgumentException(
                "The input string \"$rawValue\" for column ${this[property]} does not " +
                    "match the expected format for a decimal value",
            )
        }

        return rawValue
            .replace(".", "")
            .replace(",", ".")
            .toBigDecimal()
            .multiply(scaleFactor)
            .stripTrailingZeros()
    }

    /**
     * Tries to parse an integer value from the CSV file with the expected format
     * XXX.XXX (optional dot separation for easier readability is allowed)
     */
    fun Map<String, String>.readCsvLong(
        property: String,
        csvData: Map<String, String>,
    ): Long? {
        val rawValue = this.getCsvValueAllowingNull(property, csvData)?.trim() ?: return null
        val expectedFormat = "(\\d+(\\.)?)+".toRegex()
        if (!rawValue.matches(expectedFormat)) {
            throw IllegalArgumentException(
                "The input string \"$rawValue\" for column ${this[property]} does not " +
                    "match the expected format for an integer value",
            )
        }
        return rawValue
            .replace(".", "")
            .toLong()
    }
}
