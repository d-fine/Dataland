package org.dataland.documentmanager.services.conversion

import org.apache.tika.Tika
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.sha256
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
    private val mimeType = "application/x-tika-msoffice"

    @Test
    fun `verify that a xls file can be converted to xlsx`() {
        val testInput = MockMultipartFile(
            testFileName,
            testFileName,
            "application/vnd.ms-excel",
            TestUtils().loadFileBytes(testXls),
        )
        assertEquals(mimeType, Tika().detect(testInput.bytes))
        xlsToExcelConverter.validateFile(testInput, correlationId)
        val convertedDocument = xlsToExcelConverter.convertFile(testInput, correlationId)
        assertEquals(mimeType, Tika().detect(convertedDocument))
        assertEquals(convertedDocument.sha256(), testInput.bytes.sha256())
    }

    @Test
    fun `check that an empty xls file is not validated`() {
        val testInput = MockMultipartFile(
            testFileName,
            testFileName,
            "application/vnd.ms-excel",
            TestUtils().loadFileBytes(emptyXls),
        )
        assertEquals(mimeType, Tika().detect(testInput.bytes))
        val exception = assertThrows<InvalidInputApiException> {
            xlsToExcelConverter.validateFile(testInput, correlationId)
        }
        assertEquals("An empty spreadsheet was provided", exception.message)
    }
}
