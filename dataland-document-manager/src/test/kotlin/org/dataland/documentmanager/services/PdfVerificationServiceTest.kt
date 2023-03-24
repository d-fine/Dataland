package org.dataland.documentmanager.services

import org.apache.pdfbox.io.IOUtils
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockMultipartFile

class PdfVerificationServiceTest {
    private val pdfVerificationService = PdfVerificationService()

    @Test
    fun `verifies that a valid pdf document passes the basic checks`() {
        val testFileStream = javaClass.getResourceAsStream("samplePdfs/StandardWordExport.pdf")
        val testFileBytes = IOUtils.toByteArray(testFileStream)
        val testFile = MockMultipartFile("test.pdf", "test.pdf", "application/pdf", testFileBytes)
        pdfVerificationService.assertThatDocumentLooksLikeAPdf(testFile, "test-correlation-id")
    }

    @Test
    fun `verifies that an invalid pdf document does not pass the basic checks`() {
        val testFileStream = javaClass.getResourceAsStream("samplePdfs/EmptyExcelFile.xlsx")
        val testFileBytes = IOUtils.toByteArray(testFileStream)
        val testFile = MockMultipartFile(
            "test.xlsx",
            "test.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            testFileBytes,
        )
        assertThrows<InvalidInputApiException> {
            pdfVerificationService.assertThatDocumentLooksLikeAPdf(testFile, "test-correlation-id")
        }
    }

    @Test
    fun `verifies that a non-pdf document does not pass the basic checks`() {
        val testFileStream = javaClass.getResourceAsStream("samplePdfs/EmptyExcelFile.xlsx")
        val testFileBytes = IOUtils.toByteArray(testFileStream)
        val testFile = MockMultipartFile("test.pdf", "test.pdf", "application/pdf", testFileBytes)
        assertThrows<InvalidInputApiException> {
            pdfVerificationService.assertThatDocumentLooksLikeAPdf(testFile, "test-correlation-id")
        }
    }

    @Test
    fun `verifies that a pdf document with unallowed characters in the filename does not pass the basic checks`() {
        val testFileStream = javaClass.getResourceAsStream("samplePdfs/StandardWordExport.pdf")
        val testFileBytes = IOUtils.toByteArray(testFileStream)
        val testFile = MockMultipartFile("te/st.pdf", "te/st.pdf", "application/pdf", testFileBytes)
        assertThrows<InvalidInputApiException> {
            pdfVerificationService.assertThatDocumentLooksLikeAPdf(testFile, "test-correlation-id")
        }
    }
}
