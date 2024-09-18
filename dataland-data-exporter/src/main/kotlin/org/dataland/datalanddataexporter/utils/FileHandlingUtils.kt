package org.dataland.datalanddataexporter.utils

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Properties

/**
 * A class containing utility methods for handling files in the connection of CSV data export.
 */
object FileHandlingUtils {

    /**
     * Method to get the current timestamp in the format yyyyMMdd
     * @return the current timestamp
     */
    fun getTimestamp(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        return currentDate.format(formatter)
    }

    /**
     * Reads a transformation configuration file and returns a map of JSON paths to CSV headers.
     * @param fileName The name of the transformation configuration file
     * @return A map of JSON paths to CSV headers
     */
    fun readTransformationConfig(fileName: String): Map<String, String> {
        val props = Properties()
        props.load(this.javaClass.classLoader.getResourceAsStream(fileName))
        return props
            .map { (jsonPath, csvHeader) -> jsonPath.toString() to csvHeader.toString() }
            .toMap()
    }

    /**
     * Creates directories if they do not exist.
     * @param path The path to create
     */
    fun createDirectories(path: String) {
        val directory = File(path)
        if (!directory.exists()) {
            val success = directory.mkdirs()
            if (!success) {
                throw IOException("Failed to create directories.")
            }
        }
    }

    /**
     * Writes a CSV file.
     * @param data The data to write
     * @param outputFile The output file
     * @param headers The headers
     */
    fun writeCsv(data: List<Map<String, String>>, outputFile: File, headers: List<String>) {
        if (data.isEmpty()) return

        val csvSchemaBuilder = CsvSchema.builder()
        headers.forEach { header -> csvSchemaBuilder.addColumn(header) }
        val csvSchema = csvSchemaBuilder.build().withHeader().withColumnSeparator("|".first())

        CsvMapper().writerFor(List::class.java)
            .with(csvSchema)
            .writeValue(outputFile, data)
    }
}
