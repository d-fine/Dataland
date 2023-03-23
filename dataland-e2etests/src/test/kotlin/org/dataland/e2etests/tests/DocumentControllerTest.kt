package org.dataland.e2etests.tests

import org.awaitility.Awaitility
import org.dataland.datalandbackendutils.utils.sha256
import org.dataland.documentmanager.openApiClient.api.DocumentControllerApi
import org.dataland.documentmanager.openApiClient.infrastructure.ClientException
import org.dataland.documentmanager.openApiClient.model.DocumentMetaInfo
import org.dataland.documentmanager.openApiClient.model.DocumentQAStatus
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
        // TODO should this api logic be integrated in ApiAccessor.kt?
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        assertFalse(documentControllerClient.checkDocument(nonExistentDocumentId).documentExists)
        val metaInfo = documentControllerClient.postDocument(document)
        assertEquals(expectedHash, metaInfo.documentId)
        assertEquals(DocumentQAStatus.pending, metaInfo.qaStatus)
        assertTrue(documentControllerClient.checkDocument(metaInfo.documentId!!).documentExists)
        ensureQaCompletedAndUpdateMetadata(metaInfo)
        val downloadedFile = documentControllerClient.getDocument(metaInfo.documentId!!)
        assertEquals(expectedHash, downloadedFile.readBytes().sha256())
        assertEquals(document.name, downloadedFile.name)
    }

    /**
     * Wait until QaStatus is accepted for uploaded document or throw error. The metadata of the provided document
     * is updated in the process.
     *
     * @param metaInfo the meta info for which an update of the QAStatus should be checked and awaited
     * @return Input list of UplaodInfo but with updated metadata
     */
    private fun ensureQaCompletedAndUpdateMetadata(metaInfo: DocumentMetaInfo) {
        Awaitility.await().atMost(10, TimeUnit.SECONDS)
            .until {
                try { documentControllerClient.getDocument(metaInfo.documentId!!)
                    true } catch (e: ClientException) {
                    e.statusCode != HttpStatus.NOT_FOUND.value()
                }
            }
    }
}
