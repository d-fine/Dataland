
package org.dataland.e2etests.utils

import org.apache.poi.ss.usermodel.WorkbookFactory
import org.junit.jupiter.api.Assertions
import java.io.File
import java.io.FileWriter

object ExportTestUtils {
    private val QUOTE_REGEX = "(^\")|(\"$)".toRegex()

    fun convertExcelToCsv(
        excelFile: File,
        csvFile: File,
    ) {
        val workbook = WorkbookFactory.create(excelFile)
        FileWriter(csvFile.path).use { writer ->
            val sheet = workbook.getSheetAt(0)
            for (row in sheet) {
                val rowData =
                    row.joinToString(",") { cell ->
                        cell.toString()
                    }
                writer.append(rowData).append("\n")
            }
        }
        workbook.close()
    }

    /**
     * Provides a readable CSV file with the same name (without extension) as the given file,
     * if such a file exists. If the provided file is an excel file, it will be converted to csv.
     * Otherwise, returns the given file.
     *
     * @param exportFile The file to check for a corresponding CSV version.
     * @return A readable CSV file if it exists (excel files will be converted to csv), otherwise the originally provided file.
     */
    fun getReadableCsvFile(exportFile: File): File =
        if (exportFile.extension.lowercase() == "csv") {
            exportFile
        } else {
            val csvFile = File(exportFile.parent, "${exportFile.nameWithoutExtension}.csv")
            if (csvFile.exists()) {
                csvFile
            } else if (!csvFile.exists() && exportFile.extension.lowercase() == "xlsx") {
                convertExcelToCsv(exportFile, csvFile)
                csvFile
            } else {
                exportFile
            }
        }

    /**
     * Reads the headers from a CSV file, identifying and extracting the first meaningful line of headers.
     *
     * @param csvFile The CSV file from which the header row is to be extracted.
     * @return A list of strings representing the header column names. Returns an empty list if no valid header line is found.
     */
    fun readCsvHeaders(csvFile: File): List<String> {
        val content = csvFile.readText()

        // find the header line (ignore metadata like "sep=") and split it into individual columns
        val lines = content.replace("\\n", "\n").split("\n")
        val headerLine =
            lines.find {
                it.contains(",") && !it.contains("sep=")
            } ?: lines.find {
                it.contains(";") && !it.contains("sep=")
            }
                ?: return emptyList()

        // extract cleaned header values
        return headerLine
            .replace("\"\"", "\"")
            .replace("\\\"", "\"")
            .split(",")
            .map { header ->
                header
                    .trim()
                    .replace(QUOTE_REGEX, "")
                    .replace("\\data.", "data.")
            }
    }

    /**
     * Reads a CSV file and extracts data associated with specified companies identified by their LEIs (Legal Entity Identifiers).
     * The method processes the CSV file assuming the presence of a header row containing column names, including "companyLei".
     * It maps each company's LEI to its corresponding data value based on provided column indices.
     *
     * @param csvFile The CSV file to be read and processed.
     * @param companyLeiColumnIndex The index of the column containing the company LEI in the CSV file.
     * @param dataColumnIndex The index of the column containing the corresponding data value for each company LEI.
     * @return A map where the keys are the company LEIs and the corresponding values are the associated data extracted from the CSV.
     *         If no valid data is found, an empty map is returned.
     */
    fun readCsvDataByCompanyLei(
        csvFile: File,
        companyLeiColumnIndex: Int,
        dataColumnIndex: Int,
    ): Map<String, String> {
        val result = mutableMapOf<String, String>()

        val content = csvFile.readText()
        val lines = content.replace("\\n", "\n").split("\n")

        // find header line
        val headerLineIndex =
            lines.indexOfFirst {
                it.contains("companyLei") && !it.contains("sep=")
            }
        if (headerLineIndex == -1 || headerLineIndex >= lines.size - 1) {
            return result
        }

        // extract and clean data lines (after header)
        for (i in (headerLineIndex + 1) until lines.size) {
            val line = lines[i].trim()
            if (line.isBlank()) continue

            val cleanLine =
                line
                    .replace("\"\"", "\"")
                    .replace("\\\"", "\"")

            val values = parseCSVLine(cleanLine)

            if (values.size > maxOf(companyLeiColumnIndex, dataColumnIndex)) {
                val companyLei = values[companyLeiColumnIndex].replace(QUOTE_REGEX, "")
                val dataValue =
                    if (dataColumnIndex < values.size) {
                        values[dataColumnIndex].replace(QUOTE_REGEX, "")
                    } else {
                        ""
                    }

                result[companyLei] = dataValue.takeIf { it.isNotBlank() } ?: ""
            }
        }

        return result
    }

    /**
     * Parses a single line of CSV formatted text into a list of string values.
     *
     * The method handles quoted values, including escaped quotes within quoted sections,
     * and considers commas outside of quotes as delimiters. Each value is added to the
     * list as a separate string.
     *
     * @param line The CSV formatted the input string to parse. It may contain quoted
     *             sections and escaped characters.
     * @return A list of string values parsed from the input line. Each element in
     *         the list represents a single value from the CSV line.
     */
    private fun parseCSVLine(line: String): List<String> {
        val result = ArrayList<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]

            when {
                c == '"' -> {
                    // if there are an even number of quotes, toggle the quote flag
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++ // skip the next character since we already added it to the current
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ',' && !inQuotes -> {
                    // if we're not in quotes, then we have encountered a delimiter '
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else -> {
                    current.append(c)
                }
            }

            i++
        }

        // add the last value to the result list
        result.add(current.toString())

        return result
    }

    /**
     * Validates the provided export file to ensure it is not null and contains content.
     *
     * @param exportFile the file to be validated
     * @param errorMessage the error message to be included in assertions if validation fails
     */
    fun validateExportFile(
        exportFile: File,
        errorMessage: String,
    ) {
        Assertions.assertNotNull(exportFile, "$errorMessage: Export file should not be null")
        Assertions.assertTrue(exportFile.length() > 0, "$errorMessage: Export file should have content")
    }

    /**
     * Verify column(s) existence in headers
     * @param headers List of column headers to check
     * @param columnNamePart Pattern to search for (can be the exact column name or partial match)
     * @param shouldExist Whether the pattern should exist (true) or not (false)
     * @param contextMessage Context information for the error message
     * @return The index of the first matching column or -1 if none found
     */
    fun assertColumnPatternExists(
        headers: List<String>,
        columnNamePart: String,
        shouldExist: Boolean,
        contextMessage: String,
    ): Int {
        val matchingColumnIndexes =
            headers.mapIndexedNotNull { index, header ->
                if (header.contains(columnNamePart)) index else null
            }

        if (shouldExist) {
            Assertions.assertFalse(
                matchingColumnIndexes.isEmpty(),
                "$contextMessage: The export should include at least one column header containing '$columnNamePart'",
            )
        } else {
            Assertions.assertTrue(
                matchingColumnIndexes.isEmpty(),
                "$contextMessage: The export should NOT include any column headers containing '$columnNamePart'",
            )
        }

        return matchingColumnIndexes.firstOrNull() ?: -1
    }

    /**
     * Validates the company data based on the presence and value conditions of specific keys.
     *
     * @param companyData a map containing company data with keys as identifiers and values as associated data
     * @param companyWithNullFieldLei key for the company that is expected to have a null or empty field
     * @param companyWithNonNullFieldLei key for the company that is expected to have a non-null field with a value
     * @param exportType a string indicating the type of export being validated
     */
    fun validateCompanyData(
        companyData: Map<String, String>,
        companyWithNullFieldLei: String,
        companyWithNonNullFieldLei: String,
        exportType: String,
    ) {
        // Verify company with null field has empty value
        Assertions.assertTrue(
            companyData.containsKey(companyWithNullFieldLei),
            "$exportType export should contain data for company with null field",
        )
        Assertions.assertTrue(
            companyData[companyWithNullFieldLei]?.isEmpty() ?: false,
            "Null field value should be empty for company with null field",
        )

        // Verify company with non-null field has a value
        Assertions.assertTrue(
            companyData.containsKey(companyWithNonNullFieldLei),
            "$exportType export should contain data for company with non-null field",
        )
        Assertions.assertFalse(
            companyData[companyWithNonNullFieldLei]?.isEmpty() ?: true,
            "Null field should have a value for company with non-null field",
        )
    }
}
