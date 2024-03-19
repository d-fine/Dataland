package org.dataland.documentmanager.services

import DocxToPdfConverter
import PowerPointToPdfConverter
import org.dataland.documentmanager.services.conversion.ImageToPdfConverter
import org.dataland.documentmanager.services.conversion.PdfConverter
import org.dataland.documentmanager.services.conversion.TextToPdfConverter
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile

class PdfConverterTest {
    private val pdfConverter = PdfConverter(emptyList()) // todo change argument
    private val testPng = "sampleFiles/sample.png"
    private val testTxt = "sampleFiles/sample.txt"
    private val testWord = "sampleFiles/sample.docx"
    private val testPowerPoint = "sampleFiles/CypressTests.pptx"
    private val correlationId = "test-correlation-id"

    @Test
    fun `verify something pptx`() {
        val pptConverter = PowerPointToPdfConverter()
        val testInput = MockMultipartFile(
            "CypressTests.pptx",
            "CypressTests.pptx",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            TestUtils().loadFileBytes(testPowerPoint),
        )
        pptConverter.convert(testInput, correlationId)
    }

    @Test
    fun `verify something docx`() {
        val docxConverter = DocxToPdfConverter()
        val testInput = MockMultipartFile(
            "test.docx",
            "test.docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            TestUtils().loadFileBytes(testWord),
        )
        docxConverter.convert(testInput, correlationId)
    }

    @Test
    fun `verify that a png file can be converted to pdf`() {
        val testInput = MockMultipartFile(
            "test.png",
            "test.png",
            MediaType.IMAGE_PNG_VALUE,
            TestUtils().loadFileBytes(testPng),
        )
        ImageToPdfConverter().convert(testInput, correlationId)
    }

    @Test
    fun `verify that a txt file can be converted to pdf`() {
        val testInput = MockMultipartFile(
            "test.txt",
            "test.txt",
            MediaType.TEXT_PLAIN_VALUE,
            TestUtils().loadFileBytes(testTxt),
        )
        TextToPdfConverter().convert(testInput, correlationId)
    }

    @Test
    fun `verify that a word file can be converted to pdf`() {
        val testInput = MockMultipartFile(
            "test.docx",
            "test.docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            TestUtils().loadFileBytes(testWord),
        )
        pdfConverter.convertWordDocument(testInput, correlationId)
    }

    @Test
    fun `verify that a powerpoint file can be converted to pdf`() {
        val testInput = MockMultipartFile(
            "test.pptx",
            "test.pptx",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            TestUtils().loadFileBytes(testPowerPoint),
        )
        pdfConverter.convertPowerpoint(testInput, correlationId)
    }
}
