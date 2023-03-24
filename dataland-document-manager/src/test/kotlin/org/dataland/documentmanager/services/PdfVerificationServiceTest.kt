package org.dataland.documentmanager.services

import org.apache.pdfbox.io.IOUtils
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockMultipartFile

class PdfVerificationServiceTest {
    private val pdfVerificationService = PdfVerificationService()

    @Test
    fun `verifies that a valid pdf document passes the basic checks`() {
        val testFileBytes = loadPdfFileBytes()
        val testFile = MockMultipartFile("test.pdf", "test.pdf", "application/pdf", testFileBytes)
        pdfVerificationService.assertThatDocumentLooksLikeAPdf(testFile, "test-correlation-id")
    }

    @Test
    fun `verifies that a non-pdf document does not pass the basic checks`() {
        val testFileBytes = loadExcelFileBytes()
        val testFile = MockMultipartFile(
            "test.xlsx",
            "test.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            testFileBytes,
        )
        val thrown = assertThrows<InvalidInputApiException> {
            pdfVerificationService.assertThatDocumentLooksLikeAPdf(testFile, "test-correlation-id")
        }
        assertEquals(
            "We were unable to load the PDF document you provided." +
                    " Please ensure that the file you uploaded has not been corrupted",
              thrown.message)
    }

    @Test
    fun `verifies that an invalid pdf document does not pass the basic checks`() {
        val testFileBytes = loadExcelFileBytes()
        val testFile = MockMultipartFile("test.pdf", "test.pdf", "application/pdf", testFileBytes)
        val thrown = assertThrows<InvalidInputApiException> {
            pdfVerificationService.assertThatDocumentLooksLikeAPdf(testFile, "test-correlation-id")
        }
        assertEquals(
            "We were unable to load the PDF document you provided." +
                    " Please ensure that the file you uploaded has not been corrupted",
            thrown.message)

    }

    @Test
    fun `verifies that a pdf document with wrong name ending does not pass the basic checks`() {
        val testFileBytes = loadPdfFileBytes()
        val testFile = MockMultipartFile("test", "test", "application/pdf", testFileBytes)
        val thrown = assertThrows<InvalidInputApiException> {
            pdfVerificationService.assertThatDocumentLooksLikeAPdf(testFile, "test-correlation-id")
        }
        assertEquals("We have detected that the file does not have a name ending on '.pdf'", thrown.message)
    }

    @Test
    fun `verifies that a pdf document with forbidden characters in the filename does not pass the basic checks`() {
        val testFileBytes = loadPdfFileBytes()
        val ch = '/'
        val testFile = MockMultipartFile("te/st.pdf", "te${ch}st.pdf", "application/pdf", testFileBytes)
        val thrown = assertThrows<InvalidInputApiException> {
            pdfVerificationService.assertThatDocumentLooksLikeAPdf(testFile, "test-correlation-id")
        }
        assertEquals("We have detected that the file name contains '$ch', which is not allowed", thrown.message)
    }

    private fun loadExcelFileBytes(): ByteArray {
        val testFileStream = javaClass.getResourceAsStream("samplePdfs/EmptyExcelFile.xlsx")
        return IOUtils.toByteArray(testFileStream)
    }

    private fun loadPdfFileBytes(): ByteArray {
        val testFileStream = javaClass.getResourceAsStream("samplePdfs/StandardWordExport.pdf")
        return IOUtils.toByteArray(testFileStream)
    }
}
