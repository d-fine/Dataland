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
import org.springframework.http.HttpStatus
import java.io.File
import java.util.concurrent.TimeUnit

class DocumentControllerTest {
    private val apiAccessor = ApiAccessor()
    private val documentControllerClient = DocumentControllerApi(BASE_PATH_TO_DOCUMENT_MANAGER)

    private val document = File("./public/test-report.pdf")

    @Test
    fun `test that a dummy document can be uploaded and retrieved after successful QA`() {
        val expectedHash = document.readBytes().sha256()
        val nonExistentDocumentId = "nonExistentDocumentId"
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        assertFalse(documentControllerClient.checkDocument(nonExistentDocumentId).documentExists)
        val uploadResponse = documentControllerClient.postDocument(document)
        assertEquals(expectedHash, uploadResponse.documentId)
        assertTrue(documentControllerClient.checkDocument(uploadResponse.documentId).documentExists)
        val downloadedFile = ensureQaCompleted(uploadResponse)
        assertEquals(expectedHash, downloadedFile.readBytes().sha256())
    }

    /**
     * Wait until QaStatus is accepted for uploaded document.
     *
     * @param uploadResponse the DocumentUploadResponse document's id for which an update of the QAStatus should be
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
}
