package org.dataland.datalandbatchmanager.service

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.infrastructure.ServerException
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.SocketTimeoutException

/**
 * Class for handling the upload of the company information retrieved from GLEIF to the Dataland backend
 */
@Service
class CompanyUpload(
    @Autowired private val companyDataControllerApi: CompanyDataControllerApi,
) {
    companion object {
        const val MAX_RETRIES = 3
        const val UNAUTHORIZED_CODE = 401
        const val FORBIDDEN_CODE = 403
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Uploads a single Company to the dataland backend performing at most MAX_RETRIES retries.
     * This function absorbs errors and logs them.
     */
    fun uploadSingleCompany(
        companyInformation: CompanyInformation,
    ) {
        var counter = 0
        var shouldRetry = true
        while (shouldRetry && counter < MAX_RETRIES) {
            try {
                logger.info(
                    "Uploading company data for ${companyInformation.companyName} " +
                        "(LEI: ${companyInformation.identifiers[0].identifierValue})",
                )
                companyDataControllerApi.postCompany(companyInformation)
                shouldRetry = false
            } catch (exception: SocketTimeoutException) {
                logger.error("Unexpected timeout occurred. Response was: ${exception.message}.")
                counter++
            } catch (exception: ClientException) {
                logger.error("Unable to upload company data. Response was: ${exception.message}.")
                if (exception.statusCode == UNAUTHORIZED_CODE || exception.statusCode == FORBIDDEN_CODE) {
                    logger.error("Authorization failed, attempting to regenerate access token.")
                    counter++
                } else {
                    shouldRetry = false
                }
            } catch (exception: ServerException) {
                logger.error("Unexpected server exception. Response was: ${exception.message}.")
                counter++
            }
        }
    }
}
