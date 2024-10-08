package org.dataland.e2etests.utils
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.documentmanager.openApiClient.api.DocumentControllerApi
import org.dataland.e2etests.BASE_PATH_TO_DOCUMENT_MANAGER
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.springframework.http.HttpStatus
import java.io.File

class DocumentManagerAccessor {
    companion object {
        const val WAIT_TIME_IN_MS = 500L
        const val MAX_ATTEMPTS_TO_CHECK_DOCUMENT = 20
    }

    val documentControllerApi = DocumentControllerApi(BASE_PATH_TO_DOCUMENT_MANAGER)
    val testFiles =
        listOf(
            File("./build/resources/test/documents/some-document.pdf"),
            File("./build/resources/test/documents/some-document2.pdf"),
            File("./build/resources/test/documents/StandardWordExport.pdf"),
            File("./build/resources/test/documents/more-pdfs-in-seperate-directory/some-document.pdf"),
        )

    val jwtHelper = JwtAuthenticationHelper()

    fun uploadAllTestDocumentsAndAssurePersistence() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val documentIds = mutableListOf<String>()
        testFiles.forEach { file ->
            documentIds.add(documentControllerApi.postDocument(file).documentId)
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
}
