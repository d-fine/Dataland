package org.dataland.e2etests.utils
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.documentmanager.openApiClient.api.DocumentControllerApi
import org.dataland.e2etests.BASE_PATH_TO_DOCUMENT_MANAGER
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.springframework.http.HttpStatus
import java.io.File

class DocumentManagerAccessor {

    val documentControllerApi = DocumentControllerApi(BASE_PATH_TO_DOCUMENT_MANAGER)
    val testFiles = listOf(
        File(".\\build\\resources\\test\\documents\\some-document.pdf"),
        File(".\\build\\resources\\test\\documents\\some-document2.pdf"),
        File(".\\build\\resources\\test\\documents\\StandardWordExport.pdf"),
        File(".\\build\\resources\\test\\documents\\more-pdfs-in-seperate-directory\\some-document.pdf"),
    )

    val jwtHelper = JwtAuthenticationHelper()

    fun uploadAllTestDocumentsAndAssurePersistence() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val documentIds = mutableListOf<String>()
        testFiles.forEach {
                file ->
            documentIds.add(documentControllerApi.postDocument(file).documentId)
        }
        documentIds.forEach { documentId -> executeDocumentExistenceCheckWithRetries(documentId) }
        println("all documents posted") // todo remove
    }

    private fun executeDocumentExistenceCheckWithRetries(documentId: String) {
        val maxAttempts = 20
        var attempt = 1

        while (attempt <= maxAttempts) {
            Thread.sleep(500)
            try {
                documentControllerApi.checkDocument(documentId)
            } catch (e: ClientException) {
                if (e.statusCode != HttpStatus.NOT_FOUND.value() || attempt == maxAttempts) {
                    throw e
                }
                attempt++
            }
            break
        }
    }
}
