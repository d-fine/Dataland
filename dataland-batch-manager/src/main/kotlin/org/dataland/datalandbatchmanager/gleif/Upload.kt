package org.dataland.datalandbatchmanager.gleif

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ApiClient
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbatchmanager.service.KeycloakTokenManager
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.net.SocketTimeoutException

class Upload(
    @Autowired private val keycloakTokenManager: KeycloakTokenManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun uploadCompanies(companyInformation: List<CompanyInformation>) {
        val companyDataControllerApi = CompanyDataControllerApi(System.getenv("INTERNAL_BACKEND_URL"))
        companyInformation.forEach { uploadSingleCompany(it, companyDataControllerApi) }
    }

    private fun uploadSingleCompany(companyInformation: CompanyInformation, companyDataControllerApi: CompanyDataControllerApi) {
        var counter = 0
        val maxTries = 3
        while (counter < maxTries) {
            try {
                logger.info("Uploading company data for ${companyInformation.companyName} (LEI: ${companyInformation.identifiers[0].identifierValue})")
                companyDataControllerApi.postCompany(companyInformation)
                break
            } catch (exception: Exception) {
                logger.error("Unable to upload company data. Response was: ${exception.message}.")
                when (exception) {
                    is SocketTimeoutException -> counter++
                    is ClientException -> {
                        if (exception.statusCode == 401 || exception.statusCode == 403) {
                            ApiClient.accessToken = keycloakTokenManager.getAccessToken()
                            counter++
                        } else {
                            break
                        }
                    }
                    else -> break
                }
            }
        }
    }
}
