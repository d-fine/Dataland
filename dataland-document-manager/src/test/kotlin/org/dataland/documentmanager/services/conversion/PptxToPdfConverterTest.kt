package org.dataland.documentmanager.services.conversion

import org.apache.tika.Tika
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockMultipartFile

class PptxToPdfConverterTest {

    private val correlationId = "test-correlation-id"
    private val pptxToPdfConverter = PptxToPdfConverter("/usr/lib/libreoffice")
    private val testPowerPoint = "sampleFiles/sample.pptx"
    private val emptyPowerPoint = "sampleFiles/empty.pptx"
    private val testPpt = "sampleFiles/sample.ppt"
    private val emptyPpt = "sampleFiles/empty.ppt"
    private val testFileName = "test.pptx"

    @Test
    fun `verify that a pptx file can be converted to pdf`() {
        val testInput = MockMultipartFile(
            testFileName,
            testFileName,
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            TestUtils().loadFileBytes(testPowerPoint),
        )
        assertEquals("application/x-tika-ooxml", Tika().detect(testInput.bytes))
        pptxToPdfConverter.validateFile(testInput, correlationId)
        val convertedDocument = pptxToPdfConverter.convertFile(testInput, correlationId)
        assertEquals("application/x-tika-ooxml", Tika().detect(convertedDocument))
    }

    @Test
    fun `verify that an empty pptx file is not validated`() {
        // todo update message
        val testInput = MockMultipartFile(
            testFileName,
            testFileName,
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            TestUtils().loadFileBytes(emptyPowerPoint),
        )
        assertEquals("application/x-tika-ooxml", Tika().detect(testInput.bytes))
        val exception = assertThrows<InvalidInputApiException> {
            pptxToPdfConverter.validateFile(testInput, correlationId)
        }
        assertEquals("An empty spreadsheet was provided", exception.message)
    }

    @Test
    fun `verify that a ppt file can be converted to pdf`() {
        val testInput = MockMultipartFile(
            "sample.ppt",
            "sample.ppt",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            TestUtils().loadFileBytes(testPpt),
        )
        assertEquals("application/x-tika-msoffice", Tika().detect(testInput.bytes))
        pptxToPdfConverter.validateFile(testInput, correlationId)
        val convertedDocument = pptxToPdfConverter.convertFile(testInput, correlationId)
        assertEquals("application/x-tika-ooxml", Tika().detect(convertedDocument))
    }

    @Test
    fun `verify that an empty ppt file is not vidated`() {
        // todo update message
        val testInput = MockMultipartFile(
            "sample.ppt",
            "sample.ppt",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            TestUtils().loadFileBytes(emptyPpt),
        )
        assertEquals("application/x-tika-msoffice", Tika().detect(testInput.bytes))
        val exception = assertThrows<InvalidInputApiException> {
            pptxToPdfConverter.validateFile(testInput, correlationId)
        }
        assertEquals("An empty spreadsheet was provided", exception.message)
    }
}
