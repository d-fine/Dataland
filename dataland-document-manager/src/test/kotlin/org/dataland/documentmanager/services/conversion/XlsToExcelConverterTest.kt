package org.dataland.documentmanager.services.conversion

import org.apache.tika.Tika
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockMultipartFile

class XlsToExcelConverterTest {
    private val correlationId = "test-correlation-id"
    private val testXls = "sampleFiles/sample.xls"
    private val emptyXls = "sampleFiles/EmptyExcelFile.xls"
    private val xlsToExcelConverter = XlsToExcelConverter()
    private val testFileName = "test.xls"

    @Test
    fun `verify that a xls file can be converted to xlsx`() {
        val testInput = MockMultipartFile(
            testFileName,
            testFileName,
            "application/vnd.ms-excel",
            TestUtils().loadFileBytes(testXls),
        )
        assertEquals("application/x-tika-msoffice", Tika().detect(testInput.bytes))
        xlsToExcelConverter.validateFile(testInput, correlationId)
        val convertedDocument = xlsToExcelConverter.convertFile(testInput, correlationId)
        assertEquals("application/x-tika-msoffice", Tika().detect(convertedDocument))
    }

    @Test
    fun `check that an empty xls file is not validated`() {
        val testInput = MockMultipartFile(
            testFileName,
            testFileName,
            "application/vnd.ms-excel",
            TestUtils().loadFileBytes(emptyXls),
        )
        assertEquals("application/x-tika-msoffice", Tika().detect(testInput.bytes))
        val exception = assertThrows<InvalidInputApiException> {
            xlsToExcelConverter.validateFile(testInput, correlationId)
        }
        assertEquals("An empty spreadsheet was provided", exception.message)
    }
}
