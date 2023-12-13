package org.dataland.datalandbatchmanager.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.infrastructure.ServerException
import org.dataland.datalandbackend.openApiClient.model.CompanyInformationPatch
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.datalandbatchmanager.model.GleifCompanyInformation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.net.SocketTimeoutException

/**
 * Class for handling the upload of the company information retrieved from GLEIF to the Dataland backend
 */
@Service
class CompanyUploader(
    @Autowired private val companyDataControllerApi: CompanyDataControllerApi,
    @Autowired private val objectMapper: ObjectMapper,
) {
    companion object {
        const val MAX_RETRIES = 3
        const val UNAUTHORIZED_CODE = 401
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    @Suppress("ReturnCount")
    private fun checkForDuplicateIdentifierAndGetConflictingCompanyId(exception: ClientException): String? {
        if (exception.statusCode != HttpStatus.BAD_REQUEST.value()) {
            return null
        }

        val exceptionResponse = exception.response!! as ClientError<*>
        val exceptionBodyString = exceptionResponse.body.toString()

        val errorResponseBody = objectMapper.readTree(exceptionBodyString)
        val firstError = errorResponseBody.get("errors")?.get(0)
        if (firstError?.get("errorType")?.textValue() != "duplicate-company-identifier") {
            return null
        }

        val conflictingIdentifiers = firstError.get("metaInformation")
        if (conflictingIdentifiers == null || !conflictingIdentifiers.isArray || conflictingIdentifiers.size() != 1) {
            return null
        }

        val conflictingIdentifier = conflictingIdentifiers.get(0)
        val conflictingIdentifierType = conflictingIdentifier.get("identifierType")?.textValue()
        if (conflictingIdentifierType != IdentifierType.lei.value) {
            return null
        }

        return conflictingIdentifier.get("companyId")?.textValue()
    }

    private fun retryOnCommonApiErrors(functionToExecute: () -> Unit) {
        var counter = 0
        while (counter < MAX_RETRIES) {
            try {
                functionToExecute()
                return
            } catch (exception: ClientException) {
                logger.error("Unexpected client exception occurred. Response was: ${exception.message}.")
                counter++
            } catch (exception: SocketTimeoutException) {
                logger.error("Unexpected timeout occurred. Response was: ${exception.message}.")
                counter++
            } catch (exception: ServerException) {
                logger.error("Unexpected server exception. Response was: ${exception.message}.")
                counter++
            }
        }
        logger.error("Maximum number of retries exceeded.")
    }

    /**
     * Uploads a single Company to the dataland backend performing at most MAX_RETRIES retries.
     * If the company (identified by the LEI) already exists on dataland, it is patched instead.
     * This function absorbs errors and logs them.
     */
    fun uploadOrPatchSingleCompany(
        companyInformation: GleifCompanyInformation,
    ) {
        var patchCompanyId: String? = null
        retryOnCommonApiErrors {
            try {
                logger.info(
                    "Uploading company data for ${companyInformation.companyName} " +
                        "(LEI: ${companyInformation.lei})",
                )
                companyDataControllerApi.postCompany(companyInformation.toCompanyPost())
            } catch (exception: ClientException) {
                val conflictingCompanyId = checkForDuplicateIdentifierAndGetConflictingCompanyId(exception)
                if (conflictingCompanyId != null) {
                    patchCompanyId = conflictingCompanyId
                } else {
                    throw exception
                }
            }
        }

        patchCompanyId?.let {
            logger.info(
                "Company Data for Company ${companyInformation.companyName} (LEI: ${companyInformation.lei}) " +
                    "already present on Dataland. Proceeding to patch company with id $it",
            )
            patchSingleCompany(it, companyInformation)
        }
    }

    /**
     * Updates the information regarding a single company using the dataland-backend API.
     */
    private fun patchSingleCompany(
        companyId: String,
        companyInformation: GleifCompanyInformation,
    ) {
        retryOnCommonApiErrors {
            companyDataControllerApi.patchCompanyById(
                companyId,
                companyInformation.toCompanyPatch(),
            )
        }
    }

    /**
     * Updates the ISINs of all companies.
     * @param leiIsinMapping the delta-map with the format "LEI"->"ISIN1,ISIN2,..."
     */
    fun updateIsins(
        leiIsinMapping: Map<String, Set<String>>,
    ) {
        @Suppress("unused")
        for ((lei, newIsins) in leiIsinMapping) {
            retryOnCommonApiErrors {
                logger.info("Searching for company with LEI: $lei")
                val companyId = try {
                    companyDataControllerApi.getCompanyIdByIdentifier(IdentifierType.lei, lei).companyId
                } catch (e: ClientException) {
                    if (e.statusCode == HttpStatus.NOT_FOUND.value()) {
                        logger.error("Could not find company with LEI: $lei")
                        return@retryOnCommonApiErrors
                    }
                    throw e
                }
                logger.info("Patching company with ID: $companyId and LEI: $lei")
                updateIsinsOfCompany(newIsins, companyId)
            }
        }
    }

    private fun updateIsinsOfCompany(isins: Set<String>, companyId: String) {
        val updatedIdentifiers = mapOf(
            IdentifierType.isin.value to isins.toList(),
        )
        val companyPatch = CompanyInformationPatch(identifiers = updatedIdentifiers)
        companyDataControllerApi.patchCompanyById(
            companyId,
            companyPatch,
        )
    }
}
