package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

/**
 * Implementation of a service that can check if a companyId exists on Dataland by asking the backend microservice
 */
@Service("CompanyInfoService")
class CompanyInfoService(
    @Autowired private val companyApi: CompanyDataControllerApi,
) {
    /**
     * Checks if a companyId exists on Dataland by trying to retrieve it in the backend.
     * If it does not exist the method catches the not-found-exception from the backend and throws a
     * resource-not-found exception here in the community manager.
     * @param companyId is the companyId to check for
     * @returns nothing if the company exists or throws an resource not found exception if the company does not exists
     */
    fun checkIfCompanyIdIsValid(companyId: String) {
        try {
            return companyApi.isCompanyIdValid(companyId)
        } catch (e: ClientException) {
            if (e.statusCode == HttpStatus.NOT_FOUND.value()) {
                throw ResourceNotFoundApiException(
                    "Company not found",
                    "Dataland does not know the company ID $companyId",
                )
            } else {
                throw e
            }
        }
    }

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
            } else {
                throw e
            }
        }
    }

    /**
     * Checks if a companyId exists on Dataland by trying to retrieve it in the backend.
     * If it does not exist the method catches the not-found-exception from the backend and throws a
     * resource-not-found exception here in the community manager.
     * @param companyId is the companyId to check for
     * @returns the name of the company if available, otherwise the companyId
     */
    fun checkIfCompanyIdIsValidAndReturnNameOrId(companyId: String): String {
        try {
            return companyApi.getCompanyById(companyId).companyInformation.companyName.ifEmpty { companyId }
        } catch (e: ClientException) {
            if (e.statusCode == HttpStatus.NOT_FOUND.value()) {
                throw ResourceNotFoundApiException(
                    "Company not found",
                    "Dataland does not know the company ID $companyId",
                )
            } else {
                throw e
            }
        }
    }
}
