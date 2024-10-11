package org.dataland.documentmanager.services.conversion

import org.apache.tika.Tika
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile

class TextToPdfConverterTest {
    private val correlationId = "test-correlation-id"
    private val textToPdfConverter = TextToPdfConverter()
    private val testTxt = "sampleFiles/sample.txt"
    private val emptyTxt = "sampleFiles/emptyTxt.txt"
    private val testFileName = "test.txt"

    @Test
    fun `verify that a txt file is converted to pdf`() {
        val testInput =
            MockMultipartFile(
                testFileName,
                testFileName,
                MediaType.TEXT_PLAIN_VALUE,
                TestUtils().loadFileBytes(testTxt),
            )
        assertEquals("text/plain", Tika().detect(testInput.bytes))
        textToPdfConverter.validateFile(testInput, correlationId)
        val convertedDocument = textToPdfConverter.convertFile(testInput, correlationId)
        assertEquals("application/pdf", Tika().detect(convertedDocument))
    }

    @Test
    fun `check that an empty txt file is not validated`() {
        val testInput =
            MockMultipartFile(
                testFileName,
                testFileName,
                MediaType.TEXT_PLAIN_VALUE,
                TestUtils().loadFileBytes(emptyTxt),
            )
        assertEquals("text/plain", Tika().detect(testInput.bytes))
        val exception =
            assertThrows<InvalidInputApiException> {
                textToPdfConverter.validateFile(testInput, correlationId)
            }
        assertEquals("The file you uploaded seems to be empty.", exception.message)
    }
}
