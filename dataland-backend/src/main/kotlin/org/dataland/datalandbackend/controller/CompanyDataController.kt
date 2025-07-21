package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.CompanyApi
import org.dataland.datalandbackend.entities.BasicCompanyInformation
import org.dataland.datalandbackend.interfaces.CompanyIdAndName
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.model.companies.AggregatedFrameworkDataSummary
import org.dataland.datalandbackend.model.companies.CompanyAvailableDistinctValues
import org.dataland.datalandbackend.model.companies.CompanyId
import org.dataland.datalandbackend.model.companies.CompanyIdentifierValidationResult
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.companies.CompanyInformationPatch
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.utils.StoredCompanySearchFilter
import org.dataland.datalandbackend.services.CompanyAlterationManager
import org.dataland.datalandbackend.services.CompanyBaseManager
import org.dataland.datalandbackend.services.CompanyIdentifierManager
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackend.utils.CompanyIdentifierUtils
import org.dataland.datalandbackend.utils.DataPointUtils
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.utils.validateIsEmailAddress
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the company data endpoints
 * @param companyAlterationManager the company manager service to handle company alteration
 * @param companyQueryManager the company manager service to handle company database queries
 * @param companyIdentifierManager the service to handle company identifiers
 * @param companyBaseManager the company base manager service to handle basic information about companies
 */

@RestController
class CompanyDataController
    @Autowired
    constructor(
        private val companyAlterationManager: CompanyAlterationManager,
        private val companyQueryManager: CompanyQueryManager,
        private val companyIdentifierManager: CompanyIdentifierManager,
        private val companyBaseManager: CompanyBaseManager,
        private val dataPointUtils: DataPointUtils,
    ) : CompanyApi {
        private val logger = LoggerFactory.getLogger(javaClass)

        override fun postCompany(companyInformation: CompanyInformation): ResponseEntity<StoredCompany> {
            logger.info("Received a request to post a company with name '${companyInformation.companyName}'")
            companyInformation.companyContactDetails?.forEach { it.validateIsEmailAddress() }
            return ResponseEntity.ok(
                companyAlterationManager
                    .addCompany(companyInformation)
                    .toApiModel(),
            )
        }

        override fun getCompanies(
            searchString: String?,
            dataTypes: Set<DataType>?,
            countryCodes: Set<String>?,
            sectors: Set<String>?,
            chunkSize: Int?,
            chunkIndex: Int?,
        ): ResponseEntity<List<BasicCompanyInformation>> {
            logger.info(
                "Received a request to get basic company information with searchString='$searchString'" +
                    ", dataTypes='$dataTypes', countryCodes='$countryCodes', sectors='$sectors'",
            )
            return ResponseEntity.ok(
                companyQueryManager.getCompaniesInChunks(
                    StoredCompanySearchFilter(
                        searchString = searchString?.trim() ?: "",
                        dataTypeFilter = dataTypes?.map { it.name } ?: listOf(),
                        countryCodeFilter = countryCodes?.toList() ?: listOf(),
                        sectorFilter = sectors?.toList() ?: listOf(),
                    ),
                    chunkIndex ?: 0,
                    chunkSize,
                ),
            )
        }

        override fun getNumberOfCompanies(
            searchString: String?,
            dataTypes: Set<DataType>?,
            countryCodes: Set<String>?,
            sectors: Set<String>?,
        ): ResponseEntity<Int> {
            logger.info(
                "Received a request to get number of companies with searchString='$searchString'" +
                    ", dataTypes='$dataTypes', countryCodes='$countryCodes', sectors='$sectors'",
            )
            return ResponseEntity.ok(
                companyBaseManager.countNumberOfCompanies(
                    StoredCompanySearchFilter(
                        searchString = searchString ?: "",
                        dataTypeFilter = dataTypes?.map { it.name } ?: listOf(),
                        countryCodeFilter = countryCodes?.toList() ?: listOf(),
                        sectorFilter = sectors?.toList() ?: listOf(),
                    ),
                ),
            )
        }

        override fun getCompaniesBySearchString(
            searchString: String,
            resultLimit: Int,
        ): ResponseEntity<List<CompanyIdAndName>> =
            ResponseEntity.ok(
                companyQueryManager.searchCompaniesByNameOrIdentifierAndGetApiModel(
                    searchString,
                    resultLimit,
                ),
            )

        override fun existsIdentifier(
            identifierType: IdentifierType,
            identifier: String,
        ) {
            try {
                companyIdentifierManager.searchForCompanyIdentifier(identifierType, identifier)
            } catch (e: JpaObjectRetrievalFailureException) {
                throw ResourceNotFoundApiException(
                    CompanyIdentifierUtils.COMPANY_NOT_FOUND_SUMMARY,
                    CompanyIdentifierUtils.companyNotFoundMessage(identifierType, identifier),
                    e,
                )
            }
        }

        override fun getCompanyIdByIdentifier(
            identifierType: IdentifierType,
            identifier: String,
        ): ResponseEntity<CompanyId> {
            logger.info("Trying to retrieve company for $identifierType: $identifier")
            try {
                val companyId =
                    companyIdentifierManager.getCompanyIdByIdentifier(identifierType, identifier)
                logger.info("Retrieved company ID: $companyId")
                return ResponseEntity.ok(CompanyId(companyId))
            } catch (e: JpaObjectRetrievalFailureException) {
                logger.info(CompanyIdentifierUtils.companyNotFoundMessage(identifierType, identifier))
                throw ResourceNotFoundApiException(
                    CompanyIdentifierUtils.COMPANY_NOT_FOUND_SUMMARY,
                    CompanyIdentifierUtils.companyNotFoundMessage(identifierType, identifier),
                    e,
                )
            }
        }

        override fun getAvailableCompanySearchFilters(): ResponseEntity<CompanyAvailableDistinctValues> =
            ResponseEntity.ok(
                CompanyAvailableDistinctValues(
                    countryCodes = companyBaseManager.getDistinctCountryCodes(),
                    sectors = companyBaseManager.getDistinctSectors(),
                ),
            )

        override fun getCompanyById(companyId: String): ResponseEntity<StoredCompany> =
            ResponseEntity.ok(
                companyQueryManager
                    .getCompanyApiModelById(companyId),
            )

        override fun patchCompanyById(
            companyId: String,
            companyInformationPatch: CompanyInformationPatch,
        ): ResponseEntity<StoredCompany> {
            companyInformationPatch.companyContactDetails?.forEach { it.validateIsEmailAddress() }

            companyAlterationManager.patchCompany(companyId, companyInformationPatch)
            return ResponseEntity.ok(
                companyQueryManager
                    .getCompanyApiModelById(companyId),
            )
        }

        override fun putCompanyById(
            companyId: String,
            companyInformation: CompanyInformation,
        ): ResponseEntity<StoredCompany> {
            companyInformation.companyContactDetails?.forEach { it.validateIsEmailAddress() }
            companyAlterationManager.putCompany(companyId, companyInformation)
            return ResponseEntity.ok(
                companyQueryManager
                    .getCompanyApiModelById(companyId),
            )
        }

        override fun getTeaserCompanies(): List<String> = companyQueryManager.getTeaserCompanyIds()

        override fun getAggregatedFrameworkDataSummary(companyId: String): ResponseEntity<Map<DataType, AggregatedFrameworkDataSummary>> =
            ResponseEntity.ok(
                DataType.values.associateWith {
                    AggregatedFrameworkDataSummary(
                        (
                            companyQueryManager.getAllReportingPeriodsWithActiveDatasets(companyId, it) union
                                dataPointUtils.getAllReportingPeriodsWithActiveDataPoints(companyId, it.toString())
                        ).size.toLong(),
                    )
                },
            )

        override fun getCompanyInfo(companyId: String): ResponseEntity<CompanyInformation> =
            ResponseEntity.ok(
                companyQueryManager
                    .getCompanyApiModelById(companyId)
                    .companyInformation,
            )

        override fun isCompanyIdValid(companyId: String) {
            companyQueryManager.assertCompanyIdExists(companyId)
        }

        override fun getCompanySubsidiariesByParentId(companyId: String): ResponseEntity<List<BasicCompanyInformation>> =
            ResponseEntity.ok(
                companyQueryManager.getCompanySubsidiariesByParentId(companyId),
            )

        override fun postCompanyValidation(identifiers: List<String>): ResponseEntity<List<CompanyIdentifierValidationResult>> =
            ResponseEntity
                .ok(companyQueryManager.validateCompanyIdentifiers(identifiers))
    }
