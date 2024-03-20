package org.dataland.documentmanager.services.conversion

import org.dataland.documentmanager.services.TestUtils
import org.hibernate.validator.internal.util.Contracts.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile

class PptxToPdfConverterTest {

    private val correlationId = "test-correlation-id"
    private val pptxToPdfConverter = PptxToPdfConverter()
    private val testPowerPoint = "sampleFiles/sample.pptx"

    @Test
    fun `verify that a pptx file can be converted to pdf`() {
        val testInput = MockMultipartFile(
            "sample.pptx",
            "sample.pptx",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            TestUtils().loadFileBytes(testPowerPoint),
        )
        assertFalse(TestUtils().isPdf(testInput.bytes))
        val convertedDocument = pptxToPdfConverter.convertFile(testInput, correlationId)
        assertTrue(TestUtils().isPdf(convertedDocument), "converted document should be a pdf document")
        assertTrue(TestUtils().isNotEmptyFile(convertedDocument), "converted document should not be empty")
    }
}
