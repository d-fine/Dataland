package org.dataland.documentmanager.services

import org.dataland.documentmanager.services.conversion.DocxToPdfConverter
import org.dataland.documentmanager.services.conversion.FileConverter
import org.dataland.documentmanager.services.conversion.ImageToPdfConverter
import org.dataland.documentmanager.services.conversion.PdfConverter
import org.dataland.documentmanager.services.conversion.PptxToPdfConverter
import org.dataland.documentmanager.services.conversion.TextToPdfConverter
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile

class PdfConverterTest {
    private val converters = listOf<FileConverter>(
        DocxToPdfConverter(),
        ImageToPdfConverter(),
        PptxToPdfConverter(),
        TextToPdfConverter(),
    )
    private val pdfConverter = PdfConverter(converters)
    private val testPng = "sampleFiles/sample.png"
    private val testTxt = "sampleFiles/sample.txt"
    private val testWord = "sampleFiles/sample.docx"
    private val testPowerPoint = "sampleFiles/sample.pptx"
    private val correlationId = "test-correlation-id"

    @Test
    fun `verify that a pptx file can be converted to pdf`() {
        val testInput = MockMultipartFile(
            "sample.pptx",
            "sample.pptx",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            TestUtils().loadFileBytes(testPowerPoint),
        )
        pdfConverter.convertToPdf(testInput, correlationId)
    }

    @Test
    fun `verify that a docx file can be converted to pdf`() {
        val testInput = MockMultipartFile(
            "test.docx",
            "test.docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            TestUtils().loadFileBytes(testWord),
        )
        pdfConverter.convertToPdf(testInput, correlationId)
    }

    @Test
    fun `verify that a png file can be converted to pdf`() {
        val testInput = MockMultipartFile(
            "test.png",
            "test.png",
            MediaType.IMAGE_PNG_VALUE,
            TestUtils().loadFileBytes(testPng),
        )
        pdfConverter.convertToPdf(testInput, correlationId)
    }

    @Test
    fun `verify that a txt file can be converted to pdf`() {
        val testInput = MockMultipartFile(
            "test.txt",
            "test.txt",
            MediaType.TEXT_PLAIN_VALUE,
            TestUtils().loadFileBytes(testTxt),
        )
        pdfConverter.convertToPdf(testInput, correlationId)
    }
}
