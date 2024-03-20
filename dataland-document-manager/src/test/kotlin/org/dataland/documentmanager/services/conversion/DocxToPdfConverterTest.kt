package org.dataland.documentmanager.services.conversion

import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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
        assertFalse(TestUtils().isPdf(testInput.bytes))
        val convertedDocument = docxToPdfConverter.convertFile(testInput, correlationId)
        assertTrue(TestUtils().isPdf(convertedDocument), "converted document should be a pdf document")
        assertTrue(TestUtils().isNotEmptyFile(convertedDocument), "converted document should not be empty")
    }
}
