package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.CompanyApi
import org.dataland.datalandbackend.entities.CompanyIdentifierEntityId
import org.dataland.datalandbackend.interfaces.CompanyIdAndName
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.model.companies.AggregatedFrameworkDataSummary
import org.dataland.datalandbackend.model.companies.CompanyAvailableDistinctValues
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.companies.CompanyInformationPatch
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.CompanyIdentifierRepository
import org.dataland.datalandbackend.repositories.utils.StoredCompanySearchFilter
import org.dataland.datalandbackend.services.CompanyAlterationManager
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the company data endpoints
 * @param companyAlterationManager the company manager service to handle company alteration
 * @param companyQueryManager the company manager service to handle company database queries
 * @param companyIdentifierRepositoryInterface the company identifier repository
 */

@RestController
class CompanyDataController(
    @Autowired private val companyAlterationManager: CompanyAlterationManager,
    @Autowired private val companyQueryManager: CompanyQueryManager,
    @Autowired private val companyIdentifierRepositoryInterface: CompanyIdentifierRepository,
) : CompanyApi {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun postCompany(companyInformation: CompanyInformation): ResponseEntity<StoredCompany> {
        logger.info("Received a request to post a company with name '${companyInformation.companyName}'")
        return ResponseEntity.ok(
            companyAlterationManager.addCompany(companyInformation)
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
            companyQueryManager.searchCompaniesAndGetApiModel(
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
            companyQueryManager.searchCompaniesByNameOrIdentifierAndGetApiModel(
                searchString,
            ),
        )
    }

    override fun existsIdentifier(identifierType: IdentifierType, identifier: String) {
        try {
            companyIdentifierRepositoryInterface.getReferenceById(CompanyIdentifierEntityId(identifier, identifierType))
        } catch (e: JpaObjectRetrievalFailureException) {
            throw ResourceNotFoundApiException(
                "Company identifier does not exist",
                "Company identifier $identifier of type $identifierType does not exist",
                e,
            )
        }
    }

    @Suppress("SwallowedException")
    override fun getCompanyIdByIdentifier(identifierType: IdentifierType, identifier: String): String? {
        logger.info("Trying to retrieve company for $identifierType: $identifier")
        return try {
            companyIdentifierRepositoryInterface
                .getReferenceById(CompanyIdentifierEntityId(identifier, identifierType))
                .company?.companyId?.also {
                logger.info("Retrieved company ID: $it")
            }
        } catch (e: JpaObjectRetrievalFailureException) {
            logger.info("Could not retrieve company ID")
            null
        }
    }

    override fun getAvailableCompanySearchFilters(): ResponseEntity<CompanyAvailableDistinctValues> {
        return ResponseEntity.ok(
            CompanyAvailableDistinctValues(
                countryCodes = companyQueryManager.getDistinctCountryCodes(),
                sectors = companyQueryManager.getDistinctSectors(),
            ),
        )
    }

    override fun getCompanyById(companyId: String): ResponseEntity<StoredCompany> {
        return ResponseEntity.ok(
            companyQueryManager
                .getCompanyApiModelById(companyId, DatalandAuthentication.fromContextOrNull()),
        )
    }

    override fun patchCompanyById(
        companyId: String,
        companyInformationPatch: CompanyInformationPatch,
    ): ResponseEntity<StoredCompany> {
        companyAlterationManager.patchCompany(companyId, companyInformationPatch)
        return ResponseEntity.ok(
            companyQueryManager
                .getCompanyApiModelById(companyId, DatalandAuthentication.fromContextOrNull()),
        )
    }

    override fun putCompanyById(
        companyId: String,
        companyInformation: CompanyInformation,
    ): ResponseEntity<StoredCompany> {
        companyAlterationManager.putCompany(companyId, companyInformation)
        return ResponseEntity.ok(
            companyQueryManager
                .getCompanyApiModelById(companyId, DatalandAuthentication.fromContextOrNull()),
        )
    }

    override fun getTeaserCompanies(): List<String> {
        return companyQueryManager.getTeaserCompanyIds()
    }

    override fun getAggregatedFrameworkDataSummary(
        companyId: String,
    ): ResponseEntity<Map<DataType, AggregatedFrameworkDataSummary>> {
        return ResponseEntity.ok(
            DataType.values.associateWith {
                AggregatedFrameworkDataSummary(companyQueryManager.countActiveDatasets(companyId, it))
            },
        )
    }

    override fun getCompanyInfo(companyId: String): ResponseEntity<CompanyInformation> {
        return ResponseEntity.ok(
            companyQueryManager
                .getCompanyApiModelById(companyId, DatalandAuthentication.fromContextOrNull()).companyInformation,
        )
    }
}
