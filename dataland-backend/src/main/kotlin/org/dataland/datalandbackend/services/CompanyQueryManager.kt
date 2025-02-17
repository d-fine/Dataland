package org.dataland.datalandbackend.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.entities.BasicCompanyInformation
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.interfaces.CompanyIdAndName
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.repositories.DataMetaInformationRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackend.repositories.utils.StoredCompanySearchFilter
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of common read-only queries against company data
 * @param companyRepository  JPA for company data
 */
@Service("CompanyQueryManager")
class CompanyQueryManager(
    @Autowired private val companyRepository: StoredCompanyRepository,
    @Autowired private val dataMetaInfoRepository: DataMetaInformationRepository,
    @Value("classpath:org/dataland/datalandbackend/services/HighlightedLeis.json")
    private val highlightedLeisJsonResource: Resource,
) {
    private val highlightedCompanyIdsInMemoryStorage = ConcurrentHashMap<String, String>()
    private val highlightedLeis: Map<String, String> by lazy {
        loadHighlightedLeisFromJson()
    }

    /**
     * Loads LEI data from a JSON resource file and maps it to a Map.
     * @return A Map containing company names as keys and their corresponding LEIs as values.
     */
    private fun loadHighlightedLeisFromJson(): Map<String, String> {
        val objectMapper = jacksonObjectMapper()
        return highlightedLeisJsonResource.inputStream.use { inputStream ->
            objectMapper.readValue(inputStream)
        }
    }

    /**
     * Method to verify that a given company exists in the company store
     * @param companyId the ID of the to be verified company
     */
    fun verifyCompanyIdExists(companyId: String) {
        if (!companyRepository.existsById(companyId)) {
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
            val company =
                companyRepository
                    .searchCompanies(
                        StoredCompanySearchFilter(
                            countryCodeFilter = emptyList(),
                            dataTypeFilter = emptyList(),
                            sectorFilter = emptyList(),
                            searchString = lei,
                        ),
                    ).firstOrNull()
            company?.let {
                highlightedCompanyIdsInMemoryStorage[lei] = it.companyId
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
        verifyCompanyIdExists(companyId)
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
     * Counts the active datasets of a company and a specific data type
     * @param companyId the ID of the company
     * @param dataType the data type for which the datasets should be counted
     * @returns the number of active datasets of the specified company and data type
     */
    fun countActiveDatasets(
        companyId: String,
        dataType: DataType,
    ): Long =
        dataMetaInfoRepository.countByCompanyIdAndDataTypeAndCurrentlyActive(
            companyId,
            dataType.name,
            true,
        )
}
