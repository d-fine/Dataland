package org.dataland.frameworktoolbox.template

import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.dataland.frameworktoolbox.template.model.TemplateRow
import java.io.File

/**
 * An In-Memory representation of a single Excel-Template file
 */
class ExcelTemplate(
    val rows: MutableList<TemplateRow>,
) {
    companion object {
        /**
         * Load a csv or xlsx file to an ExcelTemplate.
         */
        fun fromFile(file: File): ExcelTemplate =
            when (file.extension) {
                "xlsx" -> fromXlsx(file)
                "csv" -> fromCsv(file)
                else -> throw IllegalArgumentException("Can only parse CSV and XLSX files. Got ${file.name}.")
            }

        /**
         * Parse an Excel Template from a xlsx file.
         */
        fun fromXlsx(xlsxFile: File): ExcelTemplate {
            val targetCsvFile =
                xlsxFile.parentFile
                    .resolve("${xlsxFile.nameWithoutExtension}.csv")

            ExcelToCsvConverter(xlsxFile, "Framework Data Model", targetCsvFile).convert()

            return fromCsv(targetCsvFile)
        }

        /**
         * Parse an Excel Template from a CSV file.
         */
        fun fromCsv(csvFile: File): ExcelTemplate {
            val csvSchema =
                CsvSchema
                    .emptySchema()
                    .withHeader()
                    .withColumnSeparator(',')
                    .withArrayElementSeparator(null)

            val iterator: MappingIterator<TemplateRow> =
                CsvMapper()
                    .registerModule(kotlinModule())
                    .readerFor(TemplateRow::class.java)
                    .with(CsvParser.Feature.SKIP_EMPTY_LINES)
                    .with(csvSchema)
                    .readValues(csvFile)

            val allEntries = iterator.asSequence().toMutableList()
            return ExcelTemplate(allEntries)
        }
    }
}
