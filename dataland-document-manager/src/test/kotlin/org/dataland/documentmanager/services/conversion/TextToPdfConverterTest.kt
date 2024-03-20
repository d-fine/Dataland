package org.dataland.documentmanager.services.conversion

import org.apache.tika.Tika
import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile

class TextToPdfConverterTest {

    private val correlationId = "test-correlation-id"
    private val textToPdfConverter = TextToPdfConverter()
    private val testTxt = "sampleFiles/sample.txt"

    @Test
    fun `verify that a txt file is converted to pdf`() {
        val testInput = MockMultipartFile(
            "test.txt",
            "test.txt",
            MediaType.TEXT_PLAIN_VALUE,
            TestUtils().loadFileBytes(testTxt),
        )
        assertEquals("text/plain", Tika().detect(testInput.bytes))
        val convertedDocument = textToPdfConverter.convertFile(testInput, correlationId)
        assertEquals("application/pdf", Tika().detect(convertedDocument))
    }
}
