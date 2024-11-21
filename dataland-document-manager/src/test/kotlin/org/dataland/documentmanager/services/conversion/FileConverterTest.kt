package org.dataland.documentmanager.services.conversion

import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.documentmanager.services.TestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile

class FileConverterTest {
    private val dummyPdfToPdfConverter =
        object : FileConverter(mapOf("pdf" to setOf(MediaType.APPLICATION_PDF_VALUE))) {
            override val logger = LoggerFactory.getLogger("TestLogger")

            override fun convert(
                file: MultipartFile,
                correlationId: String,
            ) = file.bytes
        }
    private val testPdf = "sampleFiles/sample.pdf"

    @Test
    fun `check that a proper error is thrown if no file extensions are provided`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                object : FileConverter(emptyMap()) {
                    override val logger = LoggerFactory.getLogger("TestLogger")

                    override fun convert(
                        file: MultipartFile,
                        correlationId: String,
                    ) = "test".encodeToByteArray()
                }
            }
        assertEquals("No file extension for conversion is provided.", exception.message)
    }

    @Test
    fun `check that a proper error is thrown if file extensions are not correctly formatted`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                object : FileConverter(mapOf("PNG" to setOf("mime-type"))) {
                    override val logger = LoggerFactory.getLogger("TestLogger")

                    override fun convert(
                        file: MultipartFile,
                        correlationId: String,
                    ) = "test".encodeToByteArray()
                }
            }
        assertEquals("Some file extensions are not lowercase.", exception.message)
    }

    @Test
    fun `check that no error is thrown if a file converter is correctly initialized`() {
        object : FileConverter(mapOf("png" to setOf("mime-type"))) {
            override val logger = LoggerFactory.getLogger("TestLogger")

            override fun convert(
                file: MultipartFile,
                correlationId: String,
            ) = "test".encodeToByteArray()
        }
    }

    @Test
    fun `check that an error is thrown for validating a file with content not fitting the mime type`() {
        val testFile =
            MockMultipartFile(
                "test.pdf",
                "test.pdf",
                "application/pdf",
                "This is text".encodeToByteArray(),
            )
        val exception =
            assertThrows<InvalidInputApiException> {
                dummyPdfToPdfConverter.validateFile(testFile, "")
            }
        assertEquals(
            "Only upload of documents with matching file extensions and MIME types is supported.",
            exception.message,
        )
    }

    @Test
    fun `verifies that a pdf with non alphanumeric characters passes the basic checks`() {
        val testFile =
            MockMultipartFile(
                "안녕하세요 세상.pdf",
                "안녕하세요 세상.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                TestUtils().loadFileBytes(testPdf),
            )
        dummyPdfToPdfConverter.validateFile(testFile, "")
    }

    @Test
    fun `verifies that a pdf with forbidden characters in the filename does not pass the basic checks`() {
        val forbiddenCharacter = '/'
        val testFile =
            MockMultipartFile(
                "te${forbiddenCharacter}st.pdf",
                "te${forbiddenCharacter}st.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                TestUtils().loadFileBytes(testPdf),
            )
        val thrown =
            assertThrows<InvalidInputApiException> {
                dummyPdfToPdfConverter.validateFile(testFile, "")
            }
        assertEquals(
            "Please ensure that your selected file name follows the naming convention for Windows: Avoid using " +
                "special characters like < > : \" / \\ | ? * and ensure the name does not end or begin with a space, " +
                "or end with a full stop character.",
            thrown.message,
        )
    }
}
