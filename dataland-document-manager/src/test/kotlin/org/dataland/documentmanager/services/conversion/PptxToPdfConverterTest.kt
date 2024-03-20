package org.dataland.documentmanager.services.conversion

import org.apache.tika.Tika
import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions.assertEquals
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
        assertEquals("application/x-tika-ooxml", Tika().detect(testInput.bytes))
        val convertedDocument = pptxToPdfConverter.convertFile(testInput, correlationId)
        assertEquals("application/x-tika-ooxml", Tika().detect(convertedDocument))
    }
}
