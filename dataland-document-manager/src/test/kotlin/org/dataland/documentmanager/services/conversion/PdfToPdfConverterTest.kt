package org.dataland.documentmanager.services.conversion

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.tika.Tika
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.sha256
import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockMultipartFile
import java.io.ByteArrayOutputStream

class PdfToPdfConverterTest {
    private val correlationId = "test-correlation-id"
    private val testPdf = "sampleFiles/sample.pdf"
    private val pdfToPdfConverter = PdfToPdfConverter()
    private val testFileName = "test.pdf"
    private val mimeType = "application/pdf"

    @Test
    fun `verify that the converter validates a pdf file but do not convert the pdf file`() {
        val testInput =
            MockMultipartFile(
                testFileName,
                testFileName,
                mimeType,
                TestUtils().loadFileBytes(testPdf),
            )
        assertEquals(mimeType, Tika().detect(testInput.bytes))
        pdfToPdfConverter.validateFile(testInput, correlationId)
        val convertedDocument = pdfToPdfConverter.convertFile(testInput, correlationId)
        assertEquals(mimeType, Tika().detect(convertedDocument))
        assertEquals(convertedDocument.sha256(), testInput.bytes.sha256())
    }

    @Test
    fun `check that an empty pdf file is not validated`() {
        val testInput =
            MockMultipartFile(
                testFileName,
                testFileName,
                mimeType,
                createEmptyPDFByteArray(),
            )
        val exception =
            assertThrows<InvalidInputApiException> {
                pdfToPdfConverter.validateFile(testInput, correlationId)
            }
        assertEquals("The file you uploaded seems to be empty.", exception.message)
    }

    private fun createEmptyPDFByteArray(): ByteArray {
        val document = PDDocument()
        val byteArrayOutputStream = ByteArrayOutputStream()
        document.save(byteArrayOutputStream)
        document.close()
        return byteArrayOutputStream.toByteArray()
    }
}
