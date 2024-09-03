package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

/**
 * Implementation of a service that can check if a companyId exists on Dataland by asking the backend microservice
 */
@Service("CompanyIdValidator")
class CompanyIdValidator(
    @Autowired private val companyApi: CompanyDataControllerApi,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Checks if a companyId exists on Dataland by trying to retrieve it in the backend.
     * If it does not exist the method catches the not-found-exception from the backend and throws a
     * resource-not-found exception here in the community manager.
     * @param companyId is the companyId to check for
     * @returns the name of the company if it could be found
     */
    fun checkIfCompanyIdIsValidAndReturnName(companyId: String): String {
        try {
            return companyApi.getCompanyById(companyId).companyInformation.companyName
        } catch (e: ClientException) {
            if (e.statusCode == HttpStatus.NOT_FOUND.value()) {
                throw ResourceNotFoundApiException(
                    "Company not found",
                    "Dataland does not know the company ID $companyId",
                )
            }

                else { logger.info("Error source found")
                throw e }
        }
    }
}
