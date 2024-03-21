package org.dataland.documentmanager.services.conversion

import org.apache.tika.Tika
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockMultipartFile

class ExcelToExcelConverterTest {
    private val correlationId = "test-correlation-id"
    private val testXlsx = "sampleFiles/sample.xlsx"
    private val emptyXlsx = "sampleFiles/EmptyExcelFile.xlsx"
    private val testXls = "sampleFiles/sample.xls"
    private val emptyXls = "sampleFiles/EmptyExcelFile.xls"
    private val excelToExcelConverter = ExcelToExcelConverter()
    private val testFileName = "test.xlsx"

    @Test
    fun `verify that a xlsx file can be converted to xlsx`() {
        val testInput = MockMultipartFile(
            testFileName,
            testFileName,
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            TestUtils().loadFileBytes(testXlsx),
        )
        assertEquals("application/x-tika-ooxml", Tika().detect(testInput.bytes))
        excelToExcelConverter.validateFile(testInput, correlationId)
        val convertedDocument = excelToExcelConverter.convertFile(testInput, correlationId)
        assertEquals("application/x-tika-ooxml", Tika().detect(convertedDocument))
    }

    @Test
    fun `check that an empty xlsx file is not validated`() {
        val testInput = MockMultipartFile(
            testFileName,
            testFileName,
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            TestUtils().loadFileBytes(emptyXlsx),
        )
        assertEquals("application/x-tika-ooxml", Tika().detect(testInput.bytes))
        val exception = assertThrows<InvalidInputApiException> {
            excelToExcelConverter.validateFile(testInput, correlationId)
        }
        assertEquals("An empty spreadsheet was provided", exception.message)
    }

    @Test
    fun `verify that a xls file can be converted to xlsx`() {
        // todo check that validation of xls works as expected
        val testInput = MockMultipartFile(
            "test.xls",
            "test.xls",
            "application/vnd.ms-excel",
            TestUtils().loadFileBytes(testXls),
        )
        assertEquals("application/x-tika-msoffice", Tika().detect(testInput.bytes))
        excelToExcelConverter.validateFile(testInput, correlationId)
        val convertedDocument = excelToExcelConverter.convertFile(testInput, correlationId)
        assertEquals("application/x-tika-msoffice", Tika().detect(convertedDocument))
    }

    @Test
    fun `check that an empty xls file is not validated`() {
        // todo check that validation of xls works as expected
        val testInput = MockMultipartFile(
            "test.xls",
            "test.xls",
            "application/vnd.ms-excel",
            TestUtils().loadFileBytes(emptyXls),
        )
        assertEquals("application/x-tika-msoffice", Tika().detect(testInput.bytes))
        val exception = assertThrows<InvalidInputApiException> {
            excelToExcelConverter.validateFile(testInput, correlationId)
        }
        assertEquals("An empty spreadsheet was provided", exception.message)
    }
}
