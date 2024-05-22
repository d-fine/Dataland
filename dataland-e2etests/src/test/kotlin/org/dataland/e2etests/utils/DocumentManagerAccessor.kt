package org.dataland.e2etests.utils
import org.dataland.documentmanager.openApiClient.api.ActuatorApi
import org.dataland.documentmanager.openApiClient.api.DocumentControllerApi
import org.dataland.documentmanager.openApiClient.api.TemporarilyCachedDocumentControllerApi
import org.dataland.e2etests.BASE_PATH_TO_DOCUMENT_MANAGER
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import java.io.File

class DocumentManagerAccessor {

    val actuatorApi = ActuatorApi(BASE_PATH_TO_DOCUMENT_MANAGER)

    val documentControllerApi = DocumentControllerApi(BASE_PATH_TO_DOCUMENT_MANAGER)

    val temporarilyCachedDocumentControllerApi = TemporarilyCachedDocumentControllerApi(BASE_PATH_TO_DOCUMENT_MANAGER)

    val jwtHelper = JwtAuthenticationHelper()

    fun uploadAllTestDocuments() : Unit{
        val currentDirectory = System.getProperty("user.dir") // todo remove
        println("Current working directory is $currentDirectory")

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        documentControllerApi.postDocumentWithHttpInfo(File(".\\build\\resources\\test\\documents\\some-document.pdf"))
        documentControllerApi.postDocumentWithHttpInfo(File(".\\build\\resources\\test\\documents\\some-document2.pdf"))
        documentControllerApi.postDocumentWithHttpInfo(File(".\\build\\resources\\test\\documents\\StandardWordExport.pdf"))
        documentControllerApi.postDocumentWithHttpInfo(File(".\\build\\resources\\test\\documents\\more-pdfs-in-seperate-directory\\some-document.pdf"))
        println("all documents posted") // todo remove
    }


}