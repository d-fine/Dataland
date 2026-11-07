package org.dataland.datalandcompanyservice.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.datalandbackendutils.exceptions.COMPANY_NOT_FOUND
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datalandcompanyservice.model.DatalandCompanyCandidate
import org.dataland.datalandcompanyservice.model.ProxySearchCompany
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

/**
 * Provides access to the companies stored in the Dataland backend.
 */
@Service
class DatalandCompanyService(
    private val companyDataControllerApi: CompanyDataControllerApi,
) {
    /**
     * Retrieves the company with the given ID from the backend, including the Dataland company of its parent LEI
     * (if one is stored and the parent exists on Dataland).
     * @param companyId the Dataland company ID
     * @return the company
     * @throws ResourceNotFoundApiException if the company does not exist on Dataland
     */
    fun getCompany(companyId: String): ProxySearchCompany {
        val companyInformation = getCompanyInformation(companyId)
        val parentCompanyLei = companyInformation.parentCompanyLei?.takeIf { it.isNotBlank() }
        return ProxySearchCompany(
            companyId = companyId,
            companyName = companyInformation.companyName,
            alternativeNames = companyInformation.companyAlternativeNames.orEmpty(),
            identifiers = companyInformation.identifiers.filterValues { it.isNotEmpty() },
            countryCode = companyInformation.countryCode,
            headquarters = companyInformation.headquarters,
            parentCompanyLei = parentCompanyLei,
            parentCompany = parentCompanyLei?.let { findCompanyByIdentifier(IdentifierType.Lei.name, it) },
        )
    }

    private fun getCompanyInformation(companyId: String): CompanyInformation {
        if (!ValidationUtils.isUuid(companyId)) throw companyNotFound(companyId)
        return try {
            companyDataControllerApi.getCompanyInfo(companyId)
        } catch (clientException: ClientException) {
            throw translateNotFound(companyId, clientException)
        }
    }

    /**
     * Checks whether a company with the given ID exists on Dataland.
     * @param companyId the Dataland company ID
     * @return true if the company exists, false otherwise
     */
    fun companyExists(companyId: String): Boolean {
        if (!ValidationUtils.isUuid(companyId)) return false
        return try {
            companyDataControllerApi.isCompanyIdValid(companyId)
            true
        } catch (clientException: ClientException) {
            if (clientException.statusCode == HttpStatus.NOT_FOUND.value()) {
                false
            } else {
                throw clientException
            }
        }
    }

    /**
     * Looks up the Dataland company ID of the company with the given LEI.
     * @param lei the LEI of the company
     * @return the Dataland company ID or null if no company with this LEI exists on Dataland
     */
    fun findCompanyIdByLei(lei: String): String? =
        try {
            companyDataControllerApi.getCompanyIdByIdentifier(IdentifierType.Lei, lei).companyId
        } catch (clientException: ClientException) {
            if (clientException.statusCode == HttpStatus.NOT_FOUND.value()) {
                null
            } else {
                throw clientException
            }
        }

    /**
     * Searches companies by (part of) their name.
     * @param name the search string
     * @param limit the maximum number of results
     * @return the matching companies, empty if the search string is too short or nothing matches
     */
    fun searchCompaniesByName(
        name: String,
        limit: Int,
    ): List<DatalandCompanyCandidate> =
        try {
            companyDataControllerApi
                .getCompaniesBySearchString(name.trim(), limit)
                .map { DatalandCompanyCandidate(it.companyId, it.companyName) }
        } catch (clientException: ClientException) {
            if (clientException.statusCode == HttpStatus.BAD_REQUEST.value()) emptyList() else throw clientException
        }

    /**
     * Looks up a company by an identifier of the given type.
     * @param identifierType the name of the identifier type (case-insensitive), e.g. Lei
     * @param identifier the identifier value
     * @return the company or null if the type is unknown or no such company exists
     */
    fun findCompanyByIdentifier(
        identifierType: String,
        identifier: String,
    ): DatalandCompanyCandidate? {
        val type = IdentifierType.values().firstOrNull { it.name.equals(identifierType.trim(), ignoreCase = true) }
        val companyId = type?.let { findCompanyIdByIdentifier(it, identifier.trim()) }
        return companyId?.let { DatalandCompanyCandidate(it, getCompanyInformation(it).companyName) }
    }

    private fun findCompanyIdByIdentifier(
        type: IdentifierType,
        identifier: String,
    ): String? =
        try {
            companyDataControllerApi.getCompanyIdByIdentifier(type, identifier).companyId
        } catch (clientException: ClientException) {
            if (clientException.statusCode == HttpStatus.NOT_FOUND.value()) null else throw clientException
        }

    private fun translateNotFound(
        companyId: String,
        clientException: ClientException,
    ): Exception =
        if (clientException.statusCode == HttpStatus.NOT_FOUND.value()) {
            companyNotFound(companyId, clientException)
        } else {
            clientException
        }

    private fun companyNotFound(
        companyId: String,
        cause: Throwable? = null,
    ) = ResourceNotFoundApiException(
        summary = COMPANY_NOT_FOUND,
        message = "Dataland does not know the company ID $companyId.",
        cause = cause,
    )
}
