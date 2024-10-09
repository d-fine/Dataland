package org.dataland.frameworktoolbox.template

import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.dataland.frameworktoolbox.template.model.TemplateRow
import java.io.File

typealias ExcelTemplate = ParsedExcel<TemplateRow>

/**
 * An In-Memory representation of a single Excel-Template file
 */
class ParsedExcel<T>(val rows: MutableList<T>) {

    companion object {

        const val DEFAULT_SHEET_NAME: String = "Framework Data Model"

        /**
         * Load a csv or xlsx file to an ExcelTemplate.
         */
        inline fun <reified T> fromFile(file: File): ParsedExcel<T> {
            return when (file.extension) {
                "xlsx" -> fromXlsx(file)
                "csv" -> fromCsv(file)
                else -> throw IllegalArgumentException("Can only parse CSV and XLSX files. Got ${file.name}.")
            }
        }

        /**
         * Parse an Excel Template from a xlsx file.
         */
        inline fun <reified T> fromXlsx(xlsxFile: File, sheetName: String = DEFAULT_SHEET_NAME): ParsedExcel<T> {
            val suffix = if (sheetName != DEFAULT_SHEET_NAME) "-$sheetName" else ""
            val targetCsvFile = xlsxFile.parentFile
                .resolve("${xlsxFile.nameWithoutExtension}$suffix.csv")

            ExcelToCsvConverter(xlsxFile, sheetName, targetCsvFile).convert()

            return fromCsv(targetCsvFile)
        }

        /**
         * Parse an Excel Template from a CSV file.
         */
        inline fun <reified T> fromCsv(csvFile: File): ParsedExcel<T> {
            val csvSchema = CsvSchema
                .emptySchema()
                .withHeader()
                .withColumnSeparator(',')
                .withArrayElementSeparator(null)

            val iterator: MappingIterator<T> = CsvMapper()
                .registerModule(kotlinModule())
                .readerFor(T::class.java)
                .with(CsvParser.Feature.SKIP_EMPTY_LINES)
                .with(csvSchema)
                .readValues(csvFile)

            val allEntries = iterator.asSequence().toMutableList()
            return ParsedExcel<T>(allEntries)
        }
    }
}
