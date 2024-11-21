package org.dataland.documentmanager.services.conversion

import org.apache.tika.Tika
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockMultipartFile

class PptxToPdfConverterTest {
    private val pathToLibre = "/usr/lib/libreoffice"
    private val correlationId = "test-correlation-id"
    private val pptxToPdfConverter = PptxToPdfConverter(pathToLibre)
    private val testPowerPoint = "sampleFiles/sample.pptx"
    private val emptyPowerPoint = "sampleFiles/empty.pptx"
    private val testFileName = "test.pptx"
    private val mimeType = "application/x-tika-ooxml"

    @Test
    fun `verify that a pptx file can be converted to pdf`() {
        val testInput =
            MockMultipartFile(
                testFileName,
                testFileName,
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                TestUtils().loadFileBytes(testPowerPoint),
            )
        assertEquals(mimeType, Tika().detect(testInput.bytes))
        pptxToPdfConverter.validateFile(testInput, correlationId)
        val convertedDocument = pptxToPdfConverter.convertFile(testInput, correlationId)
        assertEquals("application/pdf", Tika().detect(convertedDocument))
    }

    @Test
    fun `verify that an empty pptx file is not validated`() {
        val testInput =
            MockMultipartFile(
                testFileName,
                testFileName,
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                TestUtils().loadFileBytes(emptyPowerPoint),
            )
        assertEquals(mimeType, Tika().detect(testInput.bytes))
        val exception =
            assertThrows<InvalidInputApiException> {
                pptxToPdfConverter.validateFile(testInput, correlationId)
            }
        assertEquals("The file you uploaded seems to be empty.", exception.message)
    }
}
