package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.CompanyApi
import org.dataland.datalandbackend.entities.CompanyIdentifierEntityId
import org.dataland.datalandbackend.model.CompanyAvailableDistinctValues
import org.dataland.datalandbackend.model.CompanyIdAndName
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.CompanyIdentifierRepository
import org.dataland.datalandbackend.repositories.utils.StoredCompanySearchFilter
import org.dataland.datalandbackend.services.CompanyManager
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the company data endpoints
 * @param companyManager the company manager service to handle company information
 */

@RestController
class CompanyDataController(
    @Autowired var companyManager: CompanyManager,
    @Autowired var companyIdentifierRepository: CompanyIdentifierRepository,
) : CompanyApi {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun postCompany(companyInformation: CompanyInformation): ResponseEntity<StoredCompany> {
        logger.info("Received a request to post a company with name '${companyInformation.companyName}'")
        return ResponseEntity.ok(
            companyManager.addCompany(companyInformation)
                .toApiModel(DatalandAuthentication.fromContext()),
        )
    }

    override fun getCompanies(
        searchString: String?,
        dataTypes: Set<DataType>?,
        countryCodes: Set<String>?,
        sectors: Set<String>?,
        onlyCompanyNames: Boolean,
        onlyWithDataFromCurrentUser: Boolean,
    ): ResponseEntity<List<StoredCompany>> {
        logger.info(
            "Received a request to get companies with searchString='$searchString', onlyCompanyNames" +
                "='$onlyCompanyNames', dataTypes='$dataTypes', countryCodes='$countryCodes', sectors='$sectors', " +
                "onlyWithDataFromCurrentUser='$onlyWithDataFromCurrentUser'",
        )
        return ResponseEntity.ok(
            companyManager.searchCompaniesAndGetApiModel(
                StoredCompanySearchFilter(
                    searchString = searchString ?: "",
                    nameOnlyFilter = onlyCompanyNames,
                    dataTypeFilter = dataTypes?.map { it.name } ?: listOf(),
                    countryCodeFilter = countryCodes?.toList() ?: listOf(),
                    sectorFilter = sectors?.toList() ?: listOf(),
                    uploaderId = if (onlyWithDataFromCurrentUser) DatalandAuthentication.fromContext().userId else "",
                ),
                DatalandAuthentication.fromContextOrNull(),
            ),
        )
    }

    override fun getCompaniesBySearchString(
        searchString: String,
    ): ResponseEntity<List<CompanyIdAndName>> {
        return ResponseEntity.ok(
            companyManager.searchCompaniesByNameOrIdentifierAndGetApiModel(
                searchString,
            ),
        )
    }

    override fun existsIdentifier(identifierType: IdentifierType, identifier: String) {
        try {
            companyIdentifierRepository.getReferenceById(CompanyIdentifierEntityId(identifier, identifierType))
        } catch (e: JpaObjectRetrievalFailureException) {
            throw ResourceNotFoundApiException(
                "Company identifier does not exist",
                "Company identifier $identifier of type $identifierType does not exist",
                e,
            )
        }
    }

    override fun getAvailableCompanySearchFilters(): ResponseEntity<CompanyAvailableDistinctValues> {
        return ResponseEntity.ok(
            CompanyAvailableDistinctValues(
                countryCodes = companyManager.getDistinctCountryCodes(),
                sectors = companyManager.getDistinctSectors(),
            ),
        )
    }

    override fun getCompanyById(companyId: String): ResponseEntity<StoredCompany> {
        return ResponseEntity.ok(
            companyManager
                .getCompanyApiModelById(companyId, DatalandAuthentication.fromContextOrNull()),
        )
    }

    override fun patchCompanyById(companyId: String): ResponseEntity<StoredCompany> {
        return ResponseEntity.ok(
                companyManager
                        .getCompanyApiModelById(companyId, DatalandAuthentication.fromContextOrNull()),
        )
    }

    override fun getTeaserCompanies(): List<String> {
        return companyManager.getTeaserCompanyIds()
    }
}
