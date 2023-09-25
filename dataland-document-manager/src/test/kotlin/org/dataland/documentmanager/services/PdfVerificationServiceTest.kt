package org.dataland.documentmanager.services

import org.apache.pdfbox.io.IOUtils
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile

class PdfVerificationServiceTest {
    private val pdfVerificationService = PdfVerificationService()
    private val correlationId = "test-correlation-id"
    private val testPdfFile = "samplePdfs/StandardWordExport.pdf"
    private val testExcelFile = "samplePdfs/EmptyExcelFile.xlsx"

    @Test
    fun `verifies that a valid pdf document passes the basic checks`() {
        val testFileBytes = loadFileBytes(testPdfFile)
        val testFile = createPdfFromBytes(testFileBytes)
        pdfVerificationService.assertThatFileLooksLikeAValidPdfWithAValidName(testFile, correlationId)
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
            pdfVerificationService.assertThatFileLooksLikeAValidPdfWithAValidName(testFile, correlationId)
        }
        assertEquals(
            pdfVerificationService.pdfParsingErrorMessage,
            thrown.message,
        )
    }

    @Test
    fun `verifies that a pdf file with broken content does not pass the basic checks`() {
        val testFileBytes = loadFileBytes(testExcelFile)
        val testFile = createPdfFromBytes(testFileBytes)
        val thrown = assertThrows<InvalidInputApiException> {
            pdfVerificationService.assertThatFileLooksLikeAValidPdfWithAValidName(testFile, correlationId)
        }
        assertEquals(
            pdfVerificationService.pdfParsingErrorMessage,
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
        pdfVerificationService.assertThatFileLooksLikeAValidPdfWithAValidName(testFile, correlationId)
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
            pdfVerificationService.assertThatFileLooksLikeAValidPdfWithAValidName(testFile, correlationId)
        }
        assertEquals(
            pdfVerificationService.fileNameHasForbiddenCharactersMessage,
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
