package org.dataland.documentmanager.services.conversion

import org.apache.tika.Tika
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.sha256
import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockMultipartFile

class XlsxToExcelConverterTest {
    private val correlationId = "test-correlation-id"
    private val testXlsx = "sampleFiles/sample.xlsx"
    private val emptyXlsx = "sampleFiles/EmptyExcelFile.xlsx"
    private val xlsxToExcelConverter = XlsxToExcelConverter(mockClamAvClient())
    private val testFileName = "test.xlsx"
    private val mimeType = "application/x-tika-ooxml"

    @Test
    fun `verify that a xlsx file can be converted to xlsx`() {
        val testInput = MockMultipartFile(
            testFileName,
            testFileName,
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            TestUtils().loadFileBytes(testXlsx),
        )
        assertEquals(mimeType, Tika().detect(testInput.bytes))
        xlsxToExcelConverter.validateFile(testInput, correlationId)
        val convertedDocument = xlsxToExcelConverter.convertFile(testInput, correlationId)
        assertEquals(mimeType, Tika().detect(convertedDocument))
        assertEquals(convertedDocument.sha256(), testInput.bytes.sha256())
    }

    @Test
    fun `check that an empty xlsx file is not validated`() {
        val testInput = MockMultipartFile(
            testFileName,
            testFileName,
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            TestUtils().loadFileBytes(emptyXlsx),
        )
        assertEquals(mimeType, Tika().detect(testInput.bytes))
        val exception = assertThrows<InvalidInputApiException> {
            xlsxToExcelConverter.validateFile(testInput, correlationId)
        }
        assertEquals("An empty spreadsheet was provided", exception.message)
    }
}
