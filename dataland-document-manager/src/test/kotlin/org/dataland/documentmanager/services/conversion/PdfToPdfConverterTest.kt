package org.dataland.documentmanager.services.conversion

import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile

class PdfToPdfConverterTest {
    private val correlationId = "test-correlation-id"
    private val testPdf = "sampleFiles/sample.pdf"
    private val pdfToPdfConverter = PdfToPdfConverter()

    @Test
    fun `verify that a pdf file can be converted to pdf`() {
        val testInput = MockMultipartFile(
            "test.pdf",
            "test.pdf",
            "application/pdf",
            TestUtils().loadFileBytes(testPdf),
        )
        Assertions.assertTrue(TestUtils().isPdf(testInput.bytes))
        val convertedDocument = pdfToPdfConverter.convertFile(testInput, correlationId)
        Assertions.assertTrue(
            TestUtils().isPdf(convertedDocument),
            "converted document should be a pdf document",
        )
        Assertions.assertTrue(
            TestUtils().isNotEmptyFile(convertedDocument),
            "converted document should not be empty",
        )
    }
}
