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
     * Verify column existence in headers
     */
    fun assertColumnExists(
        headers: List<String>,
        columnName: String,
        shouldExist: Boolean,
        contextMessage: String,
    ): Int {
        val columnIndex = headers.indexOfFirst { it.contains(columnName) }

        if (shouldExist) {
            Assertions.assertTrue(
                columnIndex >= 0,
                "$contextMessage: The export should include a column for $columnName",
            )
        } else {
            Assertions.assertTrue(
                columnIndex < 0,
                "$contextMessage: The export should NOT include a column for $columnName",
            )
        }

        return columnIndex
    }

    /**
     * Read CSV data into a map by company ID
     */
    fun readCsvDataByCompanyId(
        file: File,
        companyIdColumnIndex: Int,
        targetColumnIndex: Int,
    ): Map<String, String> {
        val reader = BufferedReader(FileReader(file))
        // Skip header
        reader.readLine()

        val result = mutableMapOf<String, String>()
        var line: String? = reader.readLine()

        while (line != null) {
            val values = line.split(",")
            if (values.size > maxOf(companyIdColumnIndex, targetColumnIndex)) {
                val companyId = values[companyIdColumnIndex].trim().replace("\"", "")
                val fieldValue = values[targetColumnIndex].trim().replace("\"", "")
                result[companyId] = fieldValue
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
        companyWithNullFieldId: String,
        companyWithNonNullFieldId: String,
        exportType: String,
    ) {
        // Verify company with null field has empty value
        Assertions.assertTrue(
            companyData.containsKey(companyWithNullFieldId),
            "$exportType export should contain data for company with null field",
        )
        Assertions.assertTrue(
            companyData[companyWithNullFieldId]?.isEmpty() ?: false,
            "Null field value should be empty for company with null field",
        )

        // Verify company with non-null field has a value
        Assertions.assertTrue(
            companyData.containsKey(companyWithNonNullFieldId),
            "$exportType export should contain data for company with non-null field",
        )
        Assertions.assertFalse(
            companyData[companyWithNonNullFieldId]?.isEmpty() ?: true,
            "Null field should have a value for company with non-null field",
        )
    }
}
