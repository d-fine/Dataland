package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.annotations.DataTypesExtractor
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.model.CompanyIdAndName
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackend.repositories.utils.StoredCompanySearchFilter
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of common read-only queries against company data
 * @param companyRepository  JPA for company data
 */
@Service("CompanyQueryManager")
class CompanyQueryManager(
    @Autowired private val companyRepository: StoredCompanyRepository,
) {
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
     * Method to search for companies matching the company name or identifier
     * @param filter The filter to use during searching
     * @param viewingUser The user that is viewing the API model
     * @return list of all matching companies in Dataland
     */
    @Transactional
    fun searchCompaniesAndGetApiModel(
        filter: StoredCompanySearchFilter,
        viewingUser: DatalandAuthentication? = null,
    ): List<StoredCompany> {
        if (filter.dataTypeFilter.isEmpty()) {
            filter.dataTypeFilter = DataTypesExtractor().getAllDataTypes()
        }

        val filteredAndSortedResults = companyRepository.searchCompanies(filter)
        val sortingMap = filteredAndSortedResults.mapIndexed { index, storedCompanyEntity ->
            storedCompanyEntity.companyId to index
        }.toMap()

        val results = fetchAllStoredCompanyFields(filteredAndSortedResults).sortedBy {
            sortingMap.getValue(it.companyId)
        }

        return results.map { it.toApiModel(viewingUser) }
    }

    /**
     * Method to search for companies matching the company name or identifier
     * @param searchString the string to search for in the names or identifiers of a company
     * @return list of the first 100 matching companies in Dataland
     */
    @Transactional
    fun searchCompaniesByNameOrIdentifierAndGetApiModel(
        searchString: String,
    ): List<CompanyIdAndName> {
        return companyRepository.searchCompaniesByNameOrIdentifier(
            searchString,
        )
    }

    private fun fetchAllStoredCompanyFields(storedCompanies: List<StoredCompanyEntity>): List<StoredCompanyEntity> {
        var companiesWithFetchedFields = companyRepository.fetchIdentifiers(storedCompanies)
        companiesWithFetchedFields = companyRepository.fetchAlternativeNames(companiesWithFetchedFields)
        companiesWithFetchedFields = companyRepository.fetchCompanyAssociatedByDataland(companiesWithFetchedFields)
        return companiesWithFetchedFields
    }

    /**
     * Returns a list of available country codes across all stored companies
     */
    fun getDistinctCountryCodes(): Set<String> {
        return companyRepository.fetchDistinctCountryCodes()
    }

    /**
     * Returns a list of available sectors across all stored companies
     */
    fun getDistinctSectors(): Set<String> {
        return companyRepository.fetchDistinctSectors()
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
    fun getCompanyApiModelById(companyId: String, viewingUser: DatalandAuthentication? = null): StoredCompany {
        val searchResult = getCompanyById(companyId)
        return fetchAllStoredCompanyFields(listOf(searchResult)).first().toApiModel(viewingUser)
    }

    /**
     * Method to retrieve the list of currently set teaser company IDs
     * @return a list of company IDs that are currently labeled as teaser companies
     */
    fun getTeaserCompanyIds(): List<String> {
        return companyRepository.getAllByIsTeaserCompanyIsTrue().map { it.companyId }
    }

    /**
     * Method to check if a company is a teaser company and hence publicly available
     * @param companyId the ID of the company to be checked
     * @return a boolean signalling if the company is public or not
     */
    fun isCompanyPublic(companyId: String): Boolean {
        return getCompanyById(companyId).isTeaserCompany
    }

    //ToDO: create function getCompanyByLei
}
