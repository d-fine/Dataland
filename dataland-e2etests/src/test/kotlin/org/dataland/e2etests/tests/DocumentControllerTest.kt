package org.dataland.e2etests.tests

import org.awaitility.Awaitility
import org.dataland.datalandbackendutils.utils.sha256
import org.dataland.documentmanager.openApiClient.api.DocumentControllerApi
import org.dataland.documentmanager.openApiClient.infrastructure.ClientException
import org.dataland.documentmanager.openApiClient.model.DocumentUploadResponse
import org.dataland.e2etests.BASE_PATH_TO_DOCUMENT_MANAGER
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import java.io.File
import java.util.concurrent.TimeUnit

class DocumentControllerTest {
    private val apiAccessor = ApiAccessor()
    private val documentControllerClient = DocumentControllerApi(BASE_PATH_TO_DOCUMENT_MANAGER)

    private val pdfDocument = File("./public/test-report.pdf")
    private val xlsxDocument = File("./public/sample.xlsx")
    private val odsDocument = File("./public/sample.ods")
    private val docxDocument = File("./public/sample.docx")

    @Test
    fun `test that a dummy docx document can be uploaded and retrieved as pdf after successful QA`() {
        assertFalse(isPdf(docxDocument.readBytes()))
        val uploadResponse = uploadDocument(docxDocument)
        val downloadedFile = ensureQaCompleted(uploadResponse)
        assertTrue(isPdf(downloadedFile.readBytes()), "downloaded document is a pdf document")
    }

    @Test
    fun `test that a dummy ods document can be uploaded and retrieved after successful QA`() {
        val uploadResponse = uploadDocument(odsDocument)
        val downloadedFile = ensureQaCompleted(uploadResponse)
        assertEquals(odsDocument.readBytes().sha256(), downloadedFile.readBytes().sha256())
    }

    @Test
    fun `test that a dummy xlsx document can be uploaded and retrieved after successful QA`() {
        val uploadResponse = uploadDocument(xlsxDocument)
        val downloadedFile = ensureQaCompleted(uploadResponse)
        assertEquals(xlsxDocument.readBytes().sha256(), downloadedFile.readBytes().sha256())
    }

    @Test
    fun `test that a dummy pdf document can be uploaded and retrieved after successful QA`() {
        val uploadResponse = uploadDocument(pdfDocument)
        val downloadedFile = ensureQaCompleted(uploadResponse)
        assertEquals(pdfDocument.readBytes().sha256(), downloadedFile.readBytes().sha256())
    }

    @Test
    fun `test that a non existing document cannot be found`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val nonExistentDocumentId = "nonExistentDocumentId"
        val exception = assertThrows<ClientException> {
            documentControllerClient.checkDocument(nonExistentDocumentId)
        }
        assertEquals(HttpStatus.NOT_FOUND.value(), exception.statusCode)
    }

    /**
     * uploads a document
     * @param document document to upload
     * @returns the upload response
     */
    private fun uploadDocument(document: File): DocumentUploadResponse {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val expectedHash = document.readBytes().sha256()
        val uploadResponse = documentControllerClient.postDocument(document)
        assertEquals(expectedHash, uploadResponse.documentId)
        documentControllerClient.checkDocument(uploadResponse.documentId)
        return uploadResponse
    }

    /**
     * Wait until QaStatus is accepted for uploaded document.
     *
     * @param uploadResponse the DocumentUploadResponse document's id for which an update of the QaStatus should be
     * checked and awaited
     * @returns the received file
     */
    private fun ensureQaCompleted(uploadResponse: DocumentUploadResponse): File {
        lateinit var downloadedFile: File
        Awaitility.await().atMost(10, TimeUnit.SECONDS)
            .until {
                try {
                    downloadedFile = documentControllerClient.getDocument(uploadResponse.documentId)
                    true
                } catch (e: ClientException) {
                    e.statusCode != HttpStatus.NOT_FOUND.value()
                }
            }
        return downloadedFile
    }

    /**
     * checks if byte array represents a pdf document
     * @param byteArray byte array
     * @returns boolean if byte array represents a pdf
     */
    private fun isPdf(byteArray: ByteArray): Boolean {
        val pdfSignature = byteArrayOf(0x25, 0x50, 0x44, 0x46)
        return byteArray.size >= pdfSignature.size && byteArray.sliceArray(0 until pdfSignature.size)
            .contentEquals(pdfSignature)
    }
}
