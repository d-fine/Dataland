package org.dataland.documentmanager.services.conversion

import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions.assertTrue
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
        assertTrue(isXlsx(testInput.bytes))
        val convertedDocument = excelToExcelConverter.convert(testInput, correlationId)
        assertTrue(
            isXlsx(convertedDocument),
            "converted document should be a xlsx document",
        )
        assertTrue(
            TestUtils().isNotEmptyFile(convertedDocument),
            "converted document should not be empty",
        )
    }
    private fun isXlsx(byteArray: ByteArray): Boolean {
        val xlsxSignature = byteArrayOf(0x50, 0x4B, 0x03, 0x04)

        return byteArray.size >= xlsxSignature.size && byteArray.sliceArray(0 until xlsxSignature.size)
            .contentEquals(xlsxSignature)
    }
}
