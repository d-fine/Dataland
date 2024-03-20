package org.dataland.documentmanager.services.conversion

import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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
        assertFalse(TestUtils().isPdf(testInput.bytes))
        val convertedDocument = textToPdfConverter.convertFile(testInput, correlationId)
        assertTrue(TestUtils().isPdf(convertedDocument), "converted document should be a pdf document")
        assertTrue(TestUtils().isNotEmptyFile(convertedDocument), "converted document should not be empty")
    }
}
