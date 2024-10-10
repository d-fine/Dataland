package org.dataland.documentmanager.services.conversion

import org.apache.tika.Tika
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.sha256
import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockMultipartFile

class OdsToOdsConverterTest {
    private val correlationId = "test-correlation-id"
    private val testOds = "sampleFiles/sample.ods"
    private val emptyOds = "sampleFiles/empty.ods"
    private val testFileName = "test.ods"
    private val odsToOdsConverter = OdsToOdsConverter()
    private val mimeType = "application/vnd.oasis.opendocument.spreadsheet"

    @Test
    fun `verify that the converter validates a ods file but do not convert a ods file`() {
        val testInput =
            MockMultipartFile(
                testFileName,
                testFileName,
                mimeType,
                TestUtils().loadFileBytes(testOds),
            )
        assertEquals(mimeType, Tika().detect(testInput.bytes))
        odsToOdsConverter.validateFile(testInput, correlationId)
        val convertedDocument = odsToOdsConverter.convertFile(testInput, correlationId)
        assertEquals(mimeType, Tika().detect(convertedDocument))
        assertEquals(convertedDocument.sha256(), testInput.bytes.sha256())
    }

    @Test
    fun `check that an empty ods file is not validated`() {
        val testInput =
            MockMultipartFile(
                testFileName,
                testFileName,
                mimeType,
                TestUtils().loadFileBytes(emptyOds),
            )
        assertEquals(mimeType, Tika().detect(testInput.bytes))
        val exception =
            assertThrows<InvalidInputApiException> {
                odsToOdsConverter.validateFile(testInput, correlationId)
            }
        assertEquals("The file you uploaded seems to be empty.", exception.message)
    }
}
