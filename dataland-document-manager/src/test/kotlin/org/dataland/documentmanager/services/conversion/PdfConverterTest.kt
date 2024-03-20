package org.dataland.documentmanager.services.conversion

import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile

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
    fun `check if an error is thrown if there are file converters with overlapping file extension responsibility`() {
        val exception = assertThrows<IllegalArgumentException> {
            PdfConverter(
                listOf(
                    object : FileConverter(
                        mapOf(
                            "a" to setOf("abc"),
                            "b" to setOf("defg"),
                        ),
                    ) {
                        override val logger = LoggerFactory.getLogger("TestLogger")
                        override fun convert(file: MultipartFile, correlationId: String) = "test".encodeToByteArray()
                    },
                    object : FileConverter(
                        mapOf(
                            "b" to setOf("hijk"),
                            "c" to setOf("lmnop"),
                        ),
                    ) {
                        override val logger = LoggerFactory.getLogger("TestLogger")
                        override fun convert(file: MultipartFile, correlationId: String) = "test".encodeToByteArray()
                    },
                ),
            )
        }
        assertEquals("There are multiple file converters which target the same file extensions.", exception.message)
    }

    @Test
    fun `check if no error is thrown if the pdf converter is initialized correctly`() {
        PdfConverter(
            listOf(
                object : FileConverter(
                    mapOf(
                        "a" to setOf("abc"),
                        "b" to setOf("defg"),
                    ),
                ) {
                    override val logger = LoggerFactory.getLogger("TestLogger")
                    override fun convert(file: MultipartFile, correlationId: String) = "test".encodeToByteArray()
                },
                object : FileConverter(
                    mapOf(
                        "c" to setOf("lmnop"),
                    ),
                ) {
                    override val logger = LoggerFactory.getLogger("TestLogger")
                    override fun convert(file: MultipartFile, correlationId: String) = "test".encodeToByteArray()
                },
            ),
        )
    }

    // TODO move all the tests below

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
