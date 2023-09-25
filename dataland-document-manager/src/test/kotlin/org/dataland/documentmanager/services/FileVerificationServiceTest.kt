package org.dataland.documentmanager.services

import org.apache.pdfbox.io.IOUtils
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile

class FileVerificationServiceTest {
    private val fileVerificationService = FileVerificationService()
    private val correlationId = "test-correlation-id"
    private val testPdfFile = "samplePdfs/StandardWordExport.pdf"
    private val testExcelFile = "samplePdfs/EmptyExcelFile.xlsx"

    @Test
    fun `verifies that a valid pdf document passes the basic checks`() {
        val testFileBytes = loadFileBytes(testPdfFile)
        val testFile = createPdfFromBytes(testFileBytes)
        fileVerificationService.assertThatFileLooksLikeAPdf(testFile, correlationId)
    }

    @Test
    fun `verifies that a non pdf document does not pass the basic checks`() {
        val testFileBytes = loadFileBytes(testExcelFile)
        val testFile = MockMultipartFile(
            "test.xlsx",
            "test.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            testFileBytes,
        )
        val thrown = assertThrows<InvalidInputApiException> {
            fileVerificationService.assertThatFileLooksLikeAPdf(testFile, correlationId)
        }
        assertEquals(
            "We were unable to load the PDF document you provided." +
                " Please ensure that the file you uploaded has not been corrupted",
            thrown.message,
        )
    }

    @Test
    fun `verifies that an invalid pdf document does not pass the basic checks`() {
        val testFileBytes = loadFileBytes(testExcelFile)
        val testFile = createPdfFromBytes(testFileBytes)
        val thrown = assertThrows<InvalidInputApiException> {
            fileVerificationService.assertThatFileLooksLikeAPdf(testFile, correlationId)
        }
        assertEquals(
            "We were unable to load the PDF document you provided." +
                " Please ensure that the file you uploaded has not been corrupted",
            thrown.message,
        )
    }

    @Test
    fun `verifies that a pdf with non alphanumeric characters can be uploaded`() {
        val testFileBytes = loadFileBytes(testPdfFile)
        val testFile = MockMultipartFile(
            "안녕하세요 세상.pdf",
            "안녕하세요 세상.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            testFileBytes,
        )
        fileVerificationService.assertThatFileLooksLikeAPdf(testFile, correlationId)
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
