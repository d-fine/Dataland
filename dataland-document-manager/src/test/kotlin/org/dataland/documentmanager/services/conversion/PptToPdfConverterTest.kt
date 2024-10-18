package org.dataland.documentmanager.services.conversion

import org.apache.tika.Tika
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockMultipartFile

class PptToPdfConverterTest {
    private val pathToLibre = "/usr/lib/libreoffice"
    private val correlationId = "test-correlation-id"
    private val pptToPdfConverter = PptToPdfConverter(pathToLibre)
    private val testPpt = "sampleFiles/sample.ppt"
    private val emptyPpt = "sampleFiles/empty.ppt"
    private val testFileName = "test.ppt"

    @Test
    fun `verify that a ppt file can be converted to pdf`() {
        val testInput =
            MockMultipartFile(
                testFileName,
                testFileName,
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                TestUtils().loadFileBytes(testPpt),
            )
        Assertions.assertEquals("application/x-tika-msoffice", Tika().detect(testInput.bytes))
        pptToPdfConverter.validateFile(testInput, correlationId)
        val convertedDocument = pptToPdfConverter.convertFile(testInput, correlationId)
        Assertions.assertEquals("application/pdf", Tika().detect(convertedDocument))
    }

    @Test
    fun `verify that an empty ppt file is not validated`() {
        val testInput =
            MockMultipartFile(
                testFileName,
                testFileName,
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                TestUtils().loadFileBytes(emptyPpt),
            )
        Assertions.assertEquals("application/x-tika-msoffice", Tika().detect(testInput.bytes))
        val exception =
            assertThrows<InvalidInputApiException> {
                pptToPdfConverter.validateFile(testInput, correlationId)
            }
        Assertions.assertEquals("The file you uploaded seems to be empty.", exception.message)
    }
}
