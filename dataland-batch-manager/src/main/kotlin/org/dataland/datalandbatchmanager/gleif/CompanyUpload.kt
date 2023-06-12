package org.dataland.datalandbatchmanager.gleif

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ApiClient
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.infrastructure.ServerException
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbatchmanager.service.KeycloakTokenManager
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.net.SocketTimeoutException

const val MAX_RETRIES = 3
const val UNAUTHORIZED_CODE = 401
const val FORBIDDEN_CODE = 403

/**
 * Class for handling the upload of the company information retrieved from GLEIF to the Dataland backend
 * @param keycloakTokenManager the token manager required for access authentication
 */
class CompanyUpload(
    @Autowired private val keycloakTokenManager: KeycloakTokenManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Uploads a list of CompanyInformation objects via the backend endpoint
     * @param companyInformation the objects to be uploaded
     */
    fun uploadCompanies(companyInformation: List<CompanyInformation>) {
        val companyDataControllerApi = CompanyDataControllerApi(System.getenv("INTERNAL_BACKEND_URL"))
        companyInformation.forEach { uploadSingleCompany(it, companyDataControllerApi) }
    }

    private fun uploadSingleCompany(
        companyInformation: CompanyInformation,
        companyDataControllerApi: CompanyDataControllerApi,
    ) {
        var counter = 0
        while (counter < MAX_RETRIES) {
            try {
                logger.info(
                    "Uploading company data for ${companyInformation.companyName} " +
                        "(LEI: ${companyInformation.identifiers[0].identifierValue})",
                )
                companyDataControllerApi.postCompany(companyInformation)
                break
            } catch (exception: SocketTimeoutException) {
                logger.error("Unexpected timeout occurred. Response was: ${exception.message}.")
                counter++
            } catch (exception: ClientException) {
                logger.error("Unable to upload company data. Response was: ${exception.message}.")
                if (exception.statusCode == UNAUTHORIZED_CODE || exception.statusCode == FORBIDDEN_CODE) {
                    logger.error("Authorization failed, attempting to regenerate access token.")
                    ApiClient.accessToken = keycloakTokenManager.getAccessToken()
                    counter++
                } else {
                    counter = MAX_RETRIES
                }
            } catch (exception: ServerException) {
                logger.error("Unexpected server exception. Response was: ${exception.message}.")
                counter++
            }
        }
    }
}
