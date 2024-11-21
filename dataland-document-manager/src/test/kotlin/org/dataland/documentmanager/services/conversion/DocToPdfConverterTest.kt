package org.dataland.documentmanager.services.conversion

import org.apache.tika.Tika
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockMultipartFile

class DocToPdfConverterTest {
    private val pathToLibre = "/usr/lib/libreoffice"
    private val testDoc = "sampleFiles/sample.doc"
    private val emptyDoc = "sampleFiles/emptyDoc.doc"
    private val correlationId = "test-correlation-id"
    private val docToPdfConverter = DocToPdfConverter(pathToLibre)
    private val testFileName = "test.doc"
    private val mimeType = "application/x-tika-msoffice"

    @Test
    fun `verify that a doc file can be converted to pdf`() {
        val testInput =
            MockMultipartFile(
                testFileName,
                testFileName,
                "application/msword",
                TestUtils().loadFileBytes(testDoc),
            )
        assertEquals(mimeType, Tika().detect(testInput.bytes))
        docToPdfConverter.validateFile(testInput, correlationId)
        val convertedDocument = docToPdfConverter.convertFile(testInput, correlationId)
        assertEquals("application/pdf", Tika().detect(convertedDocument))
    }

    @Test
    fun `check that an empty doc file is not validated`() {
        val testInput =
            MockMultipartFile(
                testFileName,
                testFileName,
                "application/msword",
                TestUtils().loadFileBytes(emptyDoc),
            )
        assertEquals(mimeType, Tika().detect(testInput.bytes))
        val exception =
            assertThrows<InvalidInputApiException> {
                docToPdfConverter.validateFile(testInput, correlationId)
            }
        assertEquals("The file you uploaded seems to be empty.", exception.message)
    }
}
