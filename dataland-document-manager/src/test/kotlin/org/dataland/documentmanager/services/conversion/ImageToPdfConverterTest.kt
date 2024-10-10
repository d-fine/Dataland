package org.dataland.documentmanager.services.conversion

import org.apache.tika.Tika
import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile

class ImageToPdfConverterTest {
    private val correlationId = "test-correlation-id"
    private val testPng = "sampleFiles/sample.png"
    private val imageToPdfConverter = ImageToPdfConverter()

    @Test
    fun `verify that a png file can be converted to pdf`() {
        val testInput =
            MockMultipartFile(
                "test.png",
                "test.png",
                MediaType.IMAGE_PNG_VALUE,
                TestUtils().loadFileBytes(testPng),
            )
        assertEquals("image/png", Tika().detect(testInput.bytes))
        val convertedDocument = imageToPdfConverter.convertFile(testInput, correlationId)
        assertEquals("application/pdf", Tika().detect(convertedDocument))
    }
}
