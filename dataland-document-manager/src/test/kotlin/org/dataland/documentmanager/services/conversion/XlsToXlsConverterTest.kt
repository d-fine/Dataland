package org.dataland.documentmanager.services.conversion

import org.apache.tika.Tika
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.sha256
import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockMultipartFile

class XlsToXlsConverterTest {
    private val correlationId = "test-correlation-id"
    private val testXls = "sampleFiles/sample.xls"
    private val emptyXls = "sampleFiles/EmptyExcelFile.xls"
    private val xlsToXlsConverter = XlsToXlsConverter()
    private val testFileName = "test.xls"
    private val mimeType = "application/x-tika-msoffice"

    @Test
    fun `verify that the converter validates a xls file but do not convert a xls file`() {
        val testInput =
            MockMultipartFile(
                testFileName,
                testFileName,
                "application/vnd.ms-excel",
                TestUtils().loadFileBytes(testXls),
            )
        assertEquals(mimeType, Tika().detect(testInput.bytes))
        xlsToXlsConverter.validateFile(testInput, correlationId)
        val convertedDocument = xlsToXlsConverter.convertFile(testInput, correlationId)
        assertEquals(mimeType, Tika().detect(convertedDocument))
        assertEquals(convertedDocument.sha256(), testInput.bytes.sha256())
    }

    @Test
    fun `check that an empty xls file is not validated`() {
        val testInput =
            MockMultipartFile(
                testFileName,
                testFileName,
                "application/vnd.ms-excel",
                TestUtils().loadFileBytes(emptyXls),
            )
        assertEquals(mimeType, Tika().detect(testInput.bytes))
        val exception =
            assertThrows<InvalidInputApiException> {
                xlsToXlsConverter.validateFile(testInput, correlationId)
            }
        assertEquals("The file you uploaded seems to be empty.", exception.message)
    }
}
