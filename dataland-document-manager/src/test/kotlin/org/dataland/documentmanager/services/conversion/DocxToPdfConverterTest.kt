package org.dataland.documentmanager.services.conversion

import org.apache.tika.Tika
import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile

class DocxToPdfConverterTest {
    private val correlationId = "test-correlation-id"
    private val testWord = "sampleFiles/sample.docx"
    private val docxToPdfConverter = DocxToPdfConverter()

    @Test
    fun `verify that a docx file can be converted to pdf`() {
        val testInput = MockMultipartFile(
            "test.docx",
            "test.docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            TestUtils().loadFileBytes(testWord),
        )
        assertEquals("application/x-tika-ooxml", Tika().detect(testInput.bytes))
        val convertedDocument = docxToPdfConverter.convertFile(testInput, correlationId)
        assertEquals("application/pdf", Tika().detect(convertedDocument))
    }
}
