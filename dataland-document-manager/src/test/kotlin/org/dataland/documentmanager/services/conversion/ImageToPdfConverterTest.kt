package org.dataland.documentmanager.services.conversion

import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile

class ImageToPdfConverterTest {
    private val correlationId = "test-correlation-id"
    private val testPng = "sampleFiles/sample.png"
    private val imageToPdfConverter = ImageToPdfConverter()

    @Test
    fun `verify that a png file can be converted to pdf`() {
        val testInput = MockMultipartFile(
            "test.png",
            "test.png",
            MediaType.IMAGE_PNG_VALUE,
            TestUtils().loadFileBytes(testPng),
        )
        assertFalse(TestUtils().isPdf(testInput.bytes))
        val convertedDocument = imageToPdfConverter.convert(testInput, correlationId)
        assertTrue(TestUtils().isPdf(convertedDocument), "converted document should be a pdf document")
        assertTrue(TestUtils().isNotEmptyFile(convertedDocument), "converted document should be not empty")
    }
}
