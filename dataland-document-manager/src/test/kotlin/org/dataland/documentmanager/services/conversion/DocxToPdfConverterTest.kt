package org.dataland.documentmanager.services.conversion

import org.apache.tika.Tika
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockMultipartFile

class DocxToPdfConverterTest {
    private val pathToLibre = "/usr/lib/libreoffice"
    private val correlationId = "test-correlation-id"
    private val testDocx = "sampleFiles/sample.docx"
    private val emptyDocx = "sampleFiles/empty.docx"
    private val docxToPdfConverter = DocxToPdfConverter(pathToLibre)
    private val testFileName = "test.docx"
    private val mimeType = "application/x-tika-ooxml"

    @Test
    fun `verify that a docx file can be converted to pdf`() {
        val testInput =
            MockMultipartFile(
                testFileName,
                testFileName,
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                TestUtils().loadFileBytes(testDocx),
            )
        assertEquals(mimeType, Tika().detect(testInput.bytes))
        docxToPdfConverter.validateFile(testInput, correlationId)
        val convertedDocument = docxToPdfConverter.convertFile(testInput, correlationId)
        assertEquals("application/pdf", Tika().detect(convertedDocument))
    }

    @Test
    fun `check that an empty docx file is not validated`() {
        val testInput =
            MockMultipartFile(
                testFileName,
                testFileName,
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                TestUtils().loadFileBytes(emptyDocx),
            )
        assertEquals(mimeType, Tika().detect(testInput.bytes))
        val exception =
            assertThrows<InvalidInputApiException> {
                docxToPdfConverter.validateFile(testInput, correlationId)
            }
        assertEquals("The file you uploaded seems to be empty.", exception.message)
    }
}
