package org.dataland.documentmanager.services

import org.apache.pdfbox.io.IOUtils
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PdfVerificationServiceTest {
    private val pdfVerificationService = PdfVerificationService()

    @Test
    fun `verifies that a valid pdf document passes the basic checks`() {
        val testFileStream = javaClass.getResourceAsStream("samplePdfs/StandardWordExport.pdf")
        val testFileBytes = IOUtils.toByteArray(testFileStream)
        pdfVerificationService.assertThatABlobLooksLikeAPdfOnTheSurface(testFileBytes)
    }

    @Test
    fun `verifies that an invalid pdf document does not pass the basic checks`() {
        val testFileStream = javaClass.getResourceAsStream("samplePdfs/EmptyExcelFile.xlsx")
        val testFileBytes = IOUtils.toByteArray(testFileStream)
        assertThrows<InvalidInputApiException> {
            pdfVerificationService.assertThatABlobLooksLikeAPdfOnTheSurface(testFileBytes)
        }
    }
}