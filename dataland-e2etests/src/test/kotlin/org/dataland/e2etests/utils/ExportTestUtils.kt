package org.dataland.e2etests.utils

import org.junit.jupiter.api.Assertions
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

object ExportTestUtils {
    /**
     * Validates that an export file exists and has content
     */
    fun validateExportFile(
        exportFile: File,
        errorMessage: String,
    ) {
        Assertions.assertNotNull(exportFile, "$errorMessage: Export file should not be null")
        Assertions.assertTrue(exportFile.length() > 0, "$errorMessage: Export file should have content")
    }

    /**
     * Assert that content contains a specific value
     */
    fun assertContentContains(
        content: String,
        value: String,
        errorMessage: String,
    ) {
        Assertions.assertTrue(content.contains(value), errorMessage)
    }

    /**
     * Assert that content does not contain a specific value
     */
    fun assertContentDoesNotContain(
        content: String,
        value: String,
        errorMessage: String,
    ) {
        Assertions.assertFalse(content.contains(value), errorMessage)
    }

    /**
     * Read headers from a CSV file
     */
    fun readCsvHeaders(file: File): List<String> {
        val reader = BufferedReader(FileReader(file))
        val headerLine = reader.readLine() ?: ""
        reader.close()
        return headerLine.split(",")
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
     * Read CSV data into a map by company ID
     */
    fun readCsvDataByCompanyLei(
        file: File,
        companyLeiColumnIndex: Int,
        targetColumnIndex: Int,
    ): Map<String, String> {
        val reader = BufferedReader(FileReader(file))
        // Skip header
        reader.readLine()

        val result = mutableMapOf<String, String>()
        var line: String? = reader.readLine()

        while (line != null) {
            val values = line.split(",")
            if (values.size > maxOf(companyLeiColumnIndex, targetColumnIndex)) {
                val companyLei = values[companyLeiColumnIndex].trim().replace("\"", "")
                val fieldValue = values[targetColumnIndex].trim().replace("\"", "")
                result[companyLei] = fieldValue
            }
            line = reader.readLine()
        }

        reader.close()
        return result
    }

    /**
     * Gets a readable CSV file from the export file
     */
    fun getReadableCsvFile(exportFile: File): File =
        if (exportFile.extension.lowercase() == "csv") {
            exportFile
        } else {
            File(exportFile.parent, "${exportFile.nameWithoutExtension}.csv").also {
                if (!it.exists()) {
                    // If conversion file doesn't exist, just use the original
                    exportFile
                } else {
                    it
                }
            }
        }

    /**
     * Validates company data from an export
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
