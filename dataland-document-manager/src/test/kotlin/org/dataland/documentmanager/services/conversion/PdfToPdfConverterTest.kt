package org.dataland.documentmanager.services.conversion

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.tika.Tika
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
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

    @Test
    fun `verify that a pdf file can be converted to pdf`() {
        val testInput = MockMultipartFile(
            testFileName,
            testFileName,
            "application/pdf",
            TestUtils().loadFileBytes(testPdf),
        )
        assertEquals("application/pdf", Tika().detect(testInput.bytes))
        val convertedDocument = pdfToPdfConverter.convertFile(testInput, correlationId)
        assertEquals("application/pdf", Tika().detect(convertedDocument))
    }

    @Test
    fun `check that an empty pdf file is not validated`() {
        val testInput = MockMultipartFile(
            testFileName,
            testFileName,
            "application/pdf",
            createEmptyPDFByteArray(),
        )
        val exception = assertThrows<InvalidInputApiException> {
            pdfToPdfConverter.validateFile(testInput, correlationId)
        }
        assertEquals("The PDF you uploaded seems to have 0 pages.", exception.message)
    }

    private fun createEmptyPDFByteArray(): ByteArray {
        val document = PDDocument()
        val byteArrayOutputStream = ByteArrayOutputStream()
        document.save(byteArrayOutputStream)
        document.close()
        return byteArrayOutputStream.toByteArray()
    }
}
