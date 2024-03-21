package org.dataland.documentmanager.services.conversion

import org.apache.tika.Tika
import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile

class ExcelToExcelConverterTest {
    private val correlationId = "test-correlation-id"
    private val testXlsx = "sampleFiles/sample.xlsx"
    private val excelToExcelConverter = ExcelToExcelConverter()

    @Test
    fun `verify that a xlsx file can be converted to xlsx`() {
        val testInput = MockMultipartFile(
            "test.xlsx",
            "test.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            TestUtils().loadFileBytes(testXlsx),
        )
        assertEquals("application/x-tika-ooxml", Tika().detect(testInput.bytes))
        val convertedDocument = excelToExcelConverter.convertFile(testInput, correlationId)
        assertEquals("application/x-tika-ooxml", Tika().detect(convertedDocument))
    }

    // todo add test for xls files
}
