package org.dataland.documentmanager.services

import org.apache.pdfbox.io.IOUtils
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile

class VerificationServiceTest { // TODO to be removed
    private val correlationId = "test-correlation-id"
    private val testPdfFile = "sampleFiles/StandardWordExport.pdf"
    private val testExcelFile = "sampleFiles/EmptyExcelFile.xlsx"

    @Test
    fun `verifies that a valid pdf document passes the basic checks`() {
        val testFileBytes = loadFileBytes(testPdfFile)
        val testFile = createPdfFromBytes(testFileBytes)
        verificationService.assertThatFileLooksLikeAValidPdfWithAValidName(testFile, correlationId)
    }

    @Test
    fun `verifies that an xlsx format file does not pass the basic checks because it is not parsable as pdf`() {
        val testFileBytes = loadFileBytes(testExcelFile)
        val testFile = MockMultipartFile(
            "test.xlsx",
            "test.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            testFileBytes,
        )
        val thrown = assertThrows<InvalidInputApiException> {
            verificationService.assertThatFileLooksLikeAValidPdfWithAValidName(testFile, correlationId)
        }
        assertEquals(
            verificationService.pdfParsingErrorMessage,
            thrown.message,
        )
    }

    @Test
    fun `verifies that a pdf file with broken content does not pass the basic checks`() {
        val testFileBytes = loadFileBytes(testExcelFile)
        val testFile = createPdfFromBytes(testFileBytes)
        val thrown = assertThrows<InvalidInputApiException> {
            verificationService.assertThatFileLooksLikeAValidPdfWithAValidName(testFile, correlationId)
        }
        assertEquals(
            verificationService.pdfParsingErrorMessage,
            thrown.message,
        )
    }

    @Test
    fun `verifies that a pdf with non alphanumeric characters passes the basic checks`() {
        val testFileBytes = loadFileBytes(testPdfFile)
        val testFile = MockMultipartFile(
            "안녕하세요 세상.pdf",
            "안녕하세요 세상.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            testFileBytes,
        )
        verificationService.assertThatFileLooksLikeAValidPdfWithAValidName(testFile, correlationId)
    }

    @Test
    fun `verifies that a pdf with forbidden characters in the filename does not pass the basic checks`() {
        val testFileBytes = loadFileBytes(testPdfFile)
        val ch = '/'
        val testFile = MockMultipartFile(
            "te${ch}st.pdf",
            "te${ch}st.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            testFileBytes,
        )
        val thrown = assertThrows<InvalidInputApiException> {
            verificationService.assertThatFileLooksLikeAValidPdfWithAValidName(testFile, correlationId)
        }
        assertEquals(
            verificationService.fileNameHasForbiddenCharactersMessage,
            thrown.message,
        )
    }

    @Test
    fun `verifies that a mismatch in file extension and mime type is detected`() {
        val testFile = MockMultipartFile(
            "test.png",
            "test.png",
            MediaType.APPLICATION_PDF_VALUE,
            loadFileBytes(testPdfFile),
        )
        val thrown = assertThrows<InvalidInputApiException> {
            verificationService.validateFileType(testFile, correlationId)
        }
        assertEquals(
            verificationService.fileExtensionAndMimeTypeMismatchMessage,
            thrown.message,
        )
    }

    @Test
    fun `verifies that an unsupported type is detected`() {
        val testFile = MockMultipartFile(
            "test.json",
            "test.json",
            MediaType.APPLICATION_JSON_VALUE,
            loadFileBytes(testPdfFile),
        )
        val thrown = assertThrows<InvalidInputApiException> {
            verificationService.validateFileType(testFile, correlationId)
        }
        assertEquals(
            verificationService.typeNotSupportedMessage,
            thrown.message,
        )
    }

    private fun loadFileBytes(path: String): ByteArray {
        val testFileStream = javaClass.getResourceAsStream(path)
        return IOUtils.toByteArray(testFileStream)
    }

    private fun createPdfFromBytes(bytes: ByteArray): MockMultipartFile {
        return MockMultipartFile(
            "test.pdf",
            "test.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            bytes,
        )
    }
}
