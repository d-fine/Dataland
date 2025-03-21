package org.dataland.e2etests.utils

import org.dataland.datalandbackendutils.utils.sha256
import org.dataland.documentmanager.openApiClient.api.DocumentControllerApi
import org.dataland.documentmanager.openApiClient.infrastructure.ClientException
import org.dataland.documentmanager.openApiClient.model.DocumentMetaInfoResponse
import org.dataland.e2etests.BASE_PATH_TO_DOCUMENT_MANAGER
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.io.File

class DocumentControllerApiAccessor {
    companion object {
        const val WAIT_TIME_IN_MS = 500L
        const val MAX_ATTEMPTS_TO_CHECK_DOCUMENT = 20
    }

    val documentControllerApi = DocumentControllerApi(BASE_PATH_TO_DOCUMENT_MANAGER)

    private val logger = LoggerFactory.getLogger(DocumentControllerApiAccessor::class.java)

    private val jwtHelper = JwtAuthenticationHelper()

    private val testFiles =
        listOf(
            File("./build/resources/test/documents/some-document.pdf"),
            File("./build/resources/test/documents/some-document2.pdf"),
            File("./build/resources/test/documents/fake-fixtures/fake-fixture-pdf-1.pdf"),
            File("./build/resources/test/documents/fake-fixtures/fake-fixture-pdf-2.pdf"),
            File("./build/resources/test/documents/fake-fixtures/fake-fixture-pdf-3.pdf"),
            File("./build/resources/test/documents/fake-fixtures/fake-fixture-pdf-4.pdf"),
            File("./build/resources/test/documents/fake-fixtures/fake-fixture-pdf-5.pdf"),
            File("./build/resources/test/documents/more-pdfs-in-seperate-directory/some-document.pdf"),
        )

    fun uploadAllTestDocumentsAndAssurePersistence() {
        val documentIds = mutableListOf<String>()
        testFiles.forEach { file ->
            documentIds.add(uploadDocumentAsUser(file).documentId)
        }
        documentIds.forEach { documentId -> executeDocumentExistenceCheckWithRetries(documentId) }
    }

    private fun executeDocumentExistenceCheckWithRetries(documentId: String) {
        for (attempt in 1..MAX_ATTEMPTS_TO_CHECK_DOCUMENT) {
            Thread.sleep(WAIT_TIME_IN_MS)
            try {
                documentControllerApi.checkDocument(documentId)
                break
            } catch (e: ClientException) {
                if (e.statusCode != HttpStatus.NOT_FOUND.value() || attempt == MAX_ATTEMPTS_TO_CHECK_DOCUMENT) {
                    throw e
                }
            }
        }
    }

    fun uploadDocumentAsUser(
        document: File,
        technicalUser: TechnicalUser = TechnicalUser.Uploader,
    ): DocumentMetaInfoResponse {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(technicalUser)
        val expectedHash = document.readBytes().sha256()
        var uploadResponse: DocumentMetaInfoResponse
        try {
            uploadResponse = documentControllerApi.postDocument(document)
        } catch (clientException: ClientException) {
            if (clientException.statusCode == HttpStatus.CONFLICT.value()) {
                logger.info("Document already exists.")
                uploadResponse = DocumentMetaInfoResponse(documentId = expectedHash, uploaderId = "dummy-uploader-id")
            } else {
                throw clientException
            }
        }
        return uploadResponse
    }
}
