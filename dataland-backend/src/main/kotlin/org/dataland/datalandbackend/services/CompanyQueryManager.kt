package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.api.COMPANY_SEARCH_STRING_MIN_LENGTH
import org.dataland.datalandbackend.entities.BasicCompanyInformation
import org.dataland.datalandbackend.entities.CompanyIdentifierEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.interfaces.CompanyIdAndName
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.model.companies.CompanyIdentifierValidationResult
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.CompanyIdentifierRepository
import org.dataland.datalandbackend.repositories.DataMetaInformationRepository
import org.dataland.datalandbackend.repositories.IsinLeiRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackend.repositories.utils.StoredCompanySearchFilter
import org.dataland.datalandbackend.utils.identifiers.HighlightedCompanies.highlightedLeis
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of common read-only queries against company data
 * @param companyRepository  JPA for company data
 */
@Suppress("TooManyFunctions")
@Service("CompanyQueryManager")
class CompanyQueryManager
    @Autowired
    constructor(
        private val companyRepository: StoredCompanyRepository,
        private val dataMetaInfoRepository: DataMetaInformationRepository,
        private val companyIdentifierRepository: CompanyIdentifierRepository,
        private val isinLeiRepository: IsinLeiRepository,
        @Value("\${isin.chunk.size:10}")
        private val isinChunkSize: Int,
    ) {
        private val highlightedCompanyIdsInMemoryStorage = ConcurrentHashMap<String, String>()

        /**
         * Method to verify if a company ID represents an actual company on Dataland or not
         * @param companyId the ID of the to be verified company
         * @return a boolean signaling if the company exists or not
         */
        private fun checkCompanyIdExists(companyId: String): Boolean = companyRepository.existsById(companyId)

        /**
         * Method to verify that a given company exists in the company store
         * @param companyId the ID of the company to be verified
         * @throws ResourceNotFoundApiException if the company does not exist
         */
        @Throws(ResourceNotFoundApiException::class)
        fun assertCompanyIdExists(companyId: String) {
            if (!checkCompanyIdExists(companyId)) {
                throw ResourceNotFoundApiException("Company not found", "Dataland does not know the company ID $companyId")
            }
        }

        /**
         * Return a chunk of all companies matching the given filter. Iterating over chunkIndex will yield all results, eventually.
         *
         * @param chunkSize the package size of the records
         * @param chunkIndex the index of the chunk which is requested
         * @param filter The filter to use during searching
         * @return list of BasicCompanyInformation objects which is a subset of the entire search result
         */
        @Transactional
        fun getCompaniesInChunks(
            filter: StoredCompanySearchFilter,
            chunkIndex: Int,
            chunkSize: Int?,
        ): List<BasicCompanyInformation> {
            val offset = chunkIndex * (chunkSize ?: 0)

            return if (filter.searchStringLength == 0) {
                if (areAllDropdownFiltersDeactivated(filter)) {
                    initializeHighlightedLeisToHighlightedCompanyIdsMapping()
                    return companyRepository.getAllCompaniesWithDataset(
                        chunkSize,
                        offset,
                        highlightedCompanyIdsInMemoryStorage.values.toList(),
                    )
                } else {
                    companyRepository.searchCompaniesWithoutSearchString(filter, chunkSize, offset)
                }
            } else {
                companyRepository.searchCompanies(filter, chunkSize, offset)
            }
        }

        /**
         * Initializes the in-memory mapping of highlighted LEIs to highlighted company IDs.
         */
        private fun initializeHighlightedLeisToHighlightedCompanyIdsMapping() {
            highlightedLeis.values.forEach { lei ->
                if (highlightedCompanyIdsInMemoryStorage[lei].isNullOrEmpty()) {
                    companyRepository
                        .searchCompanies(
                            StoredCompanySearchFilter(
                                countryCodeFilter = emptyList(),
                                dataTypeFilter = emptyList(),
                                sectorFilter = emptyList(),
                                searchString = lei,
                            ),
                        ).firstOrNull()
                        ?.let { company ->
                            highlightedCompanyIdsInMemoryStorage[lei] = company.companyId
                        }
                }
            }
        }

        /**
         * Method to check if every dropdownFilter is deactivated
         * @param filter The filter to use during searching
         */
        private fun areAllDropdownFiltersDeactivated(filter: StoredCompanySearchFilter): Boolean =
            (
                filter.dataTypeFilterSize + filter.sectorFilterSize + filter.countryCodeFilterSize == 0
            )

        /**
         * Method to search for companies matching the company name or identifier
         * @param searchString the string to search for in the names or identifiers of a company
         * @return list of the first 100 matching companies in Dataland
         */
        @Transactional
        fun searchCompaniesByNameOrIdentifierAndGetApiModel(
            searchString: String,
            resultLimit: Int,
        ): List<CompanyIdAndName> =
            companyRepository.searchCompaniesByNameOrIdentifier(
                StoredCompanySearchFilter(emptyList(), emptyList(), emptyList(), searchString),
                resultLimit,
            )

        private fun fetchIsinIdentifiers(storedCompanies: List<StoredCompanyEntity>): List<StoredCompanyEntity> {
            storedCompanies.forEach { storedCompany ->
                val isinsAsStrings =
                    isinLeiRepository
                        .findByCompany(
                            storedCompany,
                            PageRequest.of(0, isinChunkSize, Sort.by("isin").ascending()),
                        ).content
                        .map { it.isin }
                isinsAsStrings.forEach {
                    storedCompany.identifiers.add(
                        CompanyIdentifierEntity(
                            identifierValue = it,
                            identifierType = IdentifierType.Isin,
                            company = storedCompany,
                        ),
                    )
                }
            }
            return storedCompanies
        }

        private fun fetchAllStoredCompanyFields(storedCompanies: List<StoredCompanyEntity>): List<StoredCompanyEntity> =
            storedCompanies
                .let { companyRepository.fetchNonIsinIdentifiers(it) }
                .let { fetchIsinIdentifiers(it) }
                .let { companyRepository.fetchAlternativeNames(it) }
                .let { companyRepository.fetchCompanyContactDetails(it) }
                .let { companyRepository.fetchCompanyAssociatedByDataland(it) }

        private fun getCompanyByIdAndAssertExistence(companyId: String): StoredCompanyEntity {
            assertCompanyIdExists(companyId)
            return companyRepository.findById(companyId).get()
        }

        /**
         * Method to retrieve information about a specific company as stored in the database (entity class)
         * @param companyId
         * @return the StoredCompanyEntity object of the retrieved company
         */
        @Transactional
        fun getCompanyById(companyId: String): StoredCompanyEntity = getCompanyByIdAndAssertExistence(companyId)

        /**
         * Method to retrieve information about a specific company that may be returned to the user (API model)
         * @param companyId
         * @return the StoredCompany object of the retrieved company
         */
        @Transactional
        fun getCompanyApiModelById(companyId: String): StoredCompany {
            val searchResult = getCompanyByIdAndAssertExistence(companyId)
            return fetchAllStoredCompanyFields(
                listOf(searchResult),
            ).first().toApiModel()
        }

        /**
         * Method to retrieve the list of currently set teaser company IDs
         * @return a list of company IDs that are currently labeled as teaser companies
         */
        @Transactional
        fun getTeaserCompanyIds(): List<String> = companyRepository.getAllByIsTeaserCompanyIsTrue().map { it.companyId }

        /**
         * Method to check if a company is a teaser company and hence publicly available
         * @param companyId the ID of the company to be checked
         * @return a boolean signalling if the company is public or not
         */
        @Transactional
        fun isCompanyPublic(companyId: String?): Boolean =
            companyId?.let {
                getCompanyByIdAndAssertExistence(it).isTeaserCompany
            } ?: false

        /**
         * Get all reporting periods for which at least one active dataset of the specified company and data type exists
         * @param companyId the ID of the company
         * @param dataType the data type for which the datasets should be counted
         * @returns the reporting periods of active datasets of the specified company and data type
         */
        @Transactional
        fun getAllReportingPeriodsWithActiveDatasets(
            companyId: String,
            dataType: DataType,
        ): Set<String> =
            dataMetaInfoRepository
                .getDistinctReportingPeriodsByCompanyIdAndDataTypeAndCurrentlyActive(
                    companyId = companyId,
                    dataType = dataType.name,
                    currentlyActive = true,
                )

        /**
         * A method to retrieve a list of subsidiaries of an ultimate parent company.
         * @param companyId identifier of the ultimate parent company in dataland
         * @return list of subsidiaries
         */
        @Transactional
        fun getCompanySubsidiariesByParentId(companyId: String): List<BasicCompanyInformation> {
            assertCompanyIdExists(companyId)
            return companyRepository.getCompanySubsidiariesByParentId(companyId)
        }

        /**
         * Build a validation result using an identifier and company information
         * @param identifier the identifier used to obtain the company information
         * @param storedCompanyEntity the entity containing the company information
         */
        private fun buildCompanyIdentifierValidationResult(
            identifier: String,
            storedCompanyEntity: StoredCompanyEntity,
        ): CompanyIdentifierValidationResult =
            CompanyIdentifierValidationResult(
                identifier = identifier,
                companyId = storedCompanyEntity.companyId,
                companyName = storedCompanyEntity.companyName,
                headquarters = storedCompanyEntity.headquarters,
                countryCode = storedCompanyEntity.countryCode,
                sector = storedCompanyEntity.sector,
                lei =
                    storedCompanyEntity.identifiers
                        .firstOrNull { it.identifierType == IdentifierType.Lei }
                        ?.identifierValue,
            )

        /**
         * Retrieve a validation result for a single company identifier
         * @param identifier a company identifier to validate
         */
        private fun getCompanyIdentifierValidationResult(identifier: String): CompanyIdentifierValidationResult =
            if (identifier.length < COMPANY_SEARCH_STRING_MIN_LENGTH) {
                CompanyIdentifierValidationResult(identifier)
            } else if (checkCompanyIdExists(identifier)) {
                buildCompanyIdentifierValidationResult(identifier, getCompanyByIdAndAssertExistence(identifier))
            } else {
                companyIdentifierRepository.getFirstByIdentifierValueIs(identifier)?.company?.let {
                    buildCompanyIdentifierValidationResult(identifier, it)
                } ?: isinLeiRepository.findByIsin(identifier)?.let {
                    val companyId = it.company?.companyId
                    if (companyId != null && checkCompanyIdExists(companyId)) {
                        buildCompanyIdentifierValidationResult(
                            identifier,
                            getCompanyByIdAndAssertExistence(companyId),
                        )
                    } else {
                        null
                    }
                } ?: CompanyIdentifierValidationResult(identifier)
            }

        /**
         * A method to validate if a given list of identifiers corresponds to a company in Dataland.
         * @param identifiers list of identifiers to validate
         * @return list of validation results
         */
        @Transactional(readOnly = true)
        fun validateCompanyIdentifiers(identifiers: List<String>): List<CompanyIdentifierValidationResult> =
            identifiers.map { getCompanyIdentifierValidationResult(it.trim()) }.distinctBy {
                it.companyInformation?.companyId ?: it.identifier
            }

        /**
         * For a collection of company IDs return a map associating each ID with the corresponding BasicCompanyInformation.
         */
        @Transactional(readOnly = true)
        fun getBasicCompanyInformationByIds(companyIds: Collection<String>): Map<String, BasicCompanyInformation?> {
            val storedCompanies = companyRepository.findAllById(companyIds).associateBy { it.companyId }
            val companyLeis =
                companyIdentifierRepository
                    .findCompanyIdentifierEntitiesByCompanyInAndIdentifierTypeIs(
                        storedCompanies.values,
                        IdentifierType.Lei,
                    ).associateBy { it.company?.companyId }

            return companyIds.associateWith { companyId ->
                storedCompanies[companyId]?.let {
                    BasicCompanyInformation(
                        companyId = companyId,
                        companyName = it.companyName,
                        headquarters = it.headquarters,
                        countryCode = it.countryCode,
                        sector = it.sector,
                        lei = companyLeis[companyId]?.identifierValue,
                    )
                }
            }
        }
    }
