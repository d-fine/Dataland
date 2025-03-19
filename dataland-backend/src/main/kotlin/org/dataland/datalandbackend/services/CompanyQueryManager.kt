package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.api.COMPANY_SEARCH_STRING_MIN_LENGTH
import org.dataland.datalandbackend.entities.BasicCompanyInformation
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.interfaces.CompanyIdAndName
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.model.companies.CompanyIdentifierValidationResult
import org.dataland.datalandbackend.repositories.CompanyIdentifierRepository
import org.dataland.datalandbackend.repositories.DataMetaInformationRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackend.repositories.utils.StoredCompanySearchFilter
import org.dataland.datalandbackend.utils.identifiers.HighlightedCompanies.highlightedLeis
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of common read-only queries against company data
 * @param companyRepository  JPA for company data
 */
@Service("CompanyQueryManager")
class CompanyQueryManager
    @Autowired
    constructor(
        private val companyRepository: StoredCompanyRepository,
        private val dataMetaInfoRepository: DataMetaInformationRepository,
        private val companyIdentifierRepository: CompanyIdentifierRepository,
    ) {
        private val highlightedCompanyIdsInMemoryStorage = ConcurrentHashMap<String, String>()

        /**
         * Method to verify if a company exists or not given an ID
         * @param companyId the ID of the to be verified company
         * @return a boolean signaling if the company exists or not
         */
        private fun doesCompanyIdExist(companyId: String): Boolean = companyRepository.existsById(companyId)

        /**
         * Method to verify that a given company exists in the company store
         * @param companyId the ID of the to be verified company
         * @throws ResourceNotFoundApiException if the company does not exist
         */
        fun assertCompanyIdExists(companyId: String) {
            if (!doesCompanyIdExist(companyId)) {
                throw ResourceNotFoundApiException("Company not found", "Dataland does not know the company ID $companyId")
            }
        }

        /**
         * Method to split the return type of method searchCompaniesAndGetApiModel into a list of lists each not exceeding
         * the given size
         * @param chunkSize the package size of the records
         * @param chunkIndex the index of the chunk which is requested
         * @param filter The filter to use during searching
         * @return list of lists each containing BasicCompanyInformation objects
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
         * Method to check if ever dropdownFilter is deactivated
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

        private fun fetchAllStoredCompanyFields(storedCompanies: List<StoredCompanyEntity>): List<StoredCompanyEntity> {
            var companiesWithFetchedFields = companyRepository.fetchIdentifiers(storedCompanies)
            companiesWithFetchedFields = companyRepository.fetchAlternativeNames(companiesWithFetchedFields)
            companiesWithFetchedFields = companyRepository.fetchCompanyContactDetails(companiesWithFetchedFields)
            companiesWithFetchedFields = companyRepository.fetchCompanyAssociatedByDataland(companiesWithFetchedFields)
            return companiesWithFetchedFields
        }

        /**
         * Method to retrieve information about a specific company
         * @param companyId
         * @return the StoredCompanyEntity object of the retrieved company
         */
        @Transactional
        fun getCompanyById(companyId: String): StoredCompanyEntity {
            assertCompanyIdExists(companyId)
            return companyRepository.findById(companyId).get()
        }

        /**
         * Method to retrieve information about a specific company
         * @param companyId
         * @return the StoredCompany object of the retrieved company
         */
        @Transactional
        fun getCompanyApiModelById(companyId: String): StoredCompany {
            val searchResult = getCompanyById(companyId)
            return fetchAllStoredCompanyFields(listOf(searchResult)).first().toApiModel()
        }

        /**
         * Method to retrieve the list of currently set teaser company IDs
         * @return a list of company IDs that are currently labeled as teaser companies
         */
        fun getTeaserCompanyIds(): List<String> = companyRepository.getAllByIsTeaserCompanyIsTrue().map { it.companyId }

        /**
         * Method to check if a company is a teaser company and hence publicly available
         * @param companyId the ID of the company to be checked
         * @return a boolean signalling if the company is public or not
         */
        fun isCompanyPublic(companyId: String): Boolean = getCompanyById(companyId).isTeaserCompany

        /**
         * Get all reporting periods for which at least one active dataset of the specified company and data type exists
         * @param companyId the ID of the company
         * @param dataType the data type for which the datasets should be counted
         * @returns the reporting periods of active datasets of the specified company and data type
         */
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
         * A method to validate if a given list of identifiers corresponds to a company in Dataland.
         * @param identifiers list of identifiers to validate
         * @return list of validation results
         */
        fun validateCompanyIdentifiers(identifiers: List<String>): List<CompanyIdentifierValidationResult> {
            val result = mutableListOf<CompanyIdentifierValidationResult>()
            val unprocessedIdentifiers = mutableListOf<String>()
            unprocessedIdentifiers.addAll(identifiers)

            identifiers.forEach { identifier ->
                if (identifier.length < COMPANY_SEARCH_STRING_MIN_LENGTH) {
                    result.add(CompanyIdentifierValidationResult(identifier))
                    unprocessedIdentifiers.remove(identifier)
                    return@forEach
                }
                if (doesCompanyIdExist(identifier)) {
                    result.add(CompanyIdentifierValidationResult(identifier, identifier, getCompanyById(identifier).companyName))
                    unprocessedIdentifiers.remove(identifier)
                }
            }

            unprocessedIdentifiers.forEach { identifier ->
                val identifierEntry = companyIdentifierRepository.getByIdentifierValueIs(identifier)
                if (identifierEntry != null) {
                    result.add(
                        CompanyIdentifierValidationResult(
                            identifier = identifier,
                            companyId = identifierEntry.company?.companyId,
                            companyName = identifierEntry.company?.companyName,
                        ),
                    )
                } else {
                    result.add(CompanyIdentifierValidationResult(identifier))
                }
            }
            return result.toSet().toList()
        }
    }
