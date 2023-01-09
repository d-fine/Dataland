package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyIdentifierEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.repositories.CompanyIdentifierRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackend.repositories.utils.StoredCompanySearchFilter
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackend.utils.IdUtils
import org.hibernate.exception.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of a company manager for Dataland
 * @param companyRepository  JPA for company data
 * @param companyIdentifierRepository JPA repository for company identifiers
 */
@Component("CompanyManager")
class CompanyManager(
    @Autowired private val companyRepository: StoredCompanyRepository,
    @Autowired private val companyIdentifierRepository: CompanyIdentifierRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Method to verify that a given company exists in the company store
     * @param companyId the ID of the to be verified company
     */
    fun verifyCompanyIdExists(companyId: String) {
        if (!companyRepository.existsById(companyId)) {
            throw ResourceNotFoundApiException("Company not found", "Dataland does not know the company ID $companyId")
        }
    }

    private fun createStoredCompanyEntityWithoutForeignReferences(
        companyId: String,
        companyInformation: CompanyInformation
    ): StoredCompanyEntity {
        val newCompanyEntity = StoredCompanyEntity(
            companyId = companyId,
            companyName = companyInformation.companyName,
            companyAlternativeNames = companyInformation.companyAlternativeNames,
            companyLegalForm = companyInformation.companyLegalForm,
            headquarters = companyInformation.headquarters,
            headquartersPostalCode = companyInformation.headquartersPostalCode,
            sector = companyInformation.sector,
            identifiers = mutableListOf(),
            dataRegisteredByDataland = mutableListOf(),
            countryCode = companyInformation.countryCode,
            isTeaserCompany = companyInformation.isTeaserCompany
        )

        return companyRepository.save(newCompanyEntity)
    }

    private fun createAndAssociateIdentifiers(
        savedCompanyEntity: StoredCompanyEntity,
        companyInformation: CompanyInformation
    ): List<CompanyIdentifierEntity> {
        val newIdentifiers = companyInformation.identifiers.map {
            CompanyIdentifierEntity(
                identifierType = it.identifierType, identifierValue = it.identifierValue,
                company = savedCompanyEntity, isNew = true,
            )
        }
        try {
            return companyIdentifierRepository.saveAllAndFlush(newIdentifiers).toList()
        } catch (ex: DataIntegrityViolationException) {
            val cause = ex.cause
            if (cause is ConstraintViolationException && cause.constraintName == "company_identifiers_pkey") {
                throw InvalidInputApiException(
                    "Company identifier already used",
                    "Could not insert company as one company identifier is already used to identify another company"
                )
            }
            throw ex
        }
    }

    /**
     * Method to add a company
     * @param companyInformation denotes information of the company
     * @return information of the newly created entry in the company data store of Dataland,
     * including the generated company ID
     */
    @Transactional
    fun addCompany(companyInformation: CompanyInformation): StoredCompanyEntity {
        val companyId = IdUtils.generateUUID()
        logger.info("Creating Company ${companyInformation.companyName} with ID $companyId")
        val savedCompany = createStoredCompanyEntityWithoutForeignReferences(companyId, companyInformation)
        val identifiers = createAndAssociateIdentifiers(savedCompany, companyInformation)
        savedCompany.identifiers = identifiers.toMutableList()
        logger.info("Company ${companyInformation.companyName} with ID $companyId saved to database.")
        return savedCompany
    }

    /**
     * Method to search for companies matching the company name or identifier
     * @param searchString string used for substring matching against the company name and/or identifiers
     * @param onlyCompanyNames boolean determining if the search should be solely against the company names
     * @param dataTypeFilter if not empty, return only companies that have data reported for
     * one of the specified dataTypes
     * @param countryCodeFilter set of strings with ISO country codes to return companies whose headquarters are in
     * the country of one of those ISO country codes
     * @param sectorFilter set of strings with sector names to return companies which operate in one of those sectors
     * @return list of all matching companies in Dataland
     */
    @Transactional
    fun searchCompanies(
        searchString: String = "",
        onlyCompanyNames: Boolean = false,
        dataTypeFilter: Set<DataType> = setOf(),
        countryCodeFilter: Set<String> = setOf(),
        sectorFilter: Set<String> = setOf(),
    ): List<StoredCompany> {
        val searchFilter = StoredCompanySearchFilter(
            searchString = searchString,
            nameOnlyFilter = onlyCompanyNames,
            dataTypeFilter = dataTypeFilter.map { it.name },
            sectorFilter = sectorFilter.toList(),
            countryCodeFilter = countryCodeFilter.toList(),
        )
        val filteredAndSortedResults = companyRepository.searchCompanies(searchFilter)
        val sortingMap = filteredAndSortedResults.mapIndexed {
                index, storedCompanyEntity ->
            storedCompanyEntity.companyId to index
        }.toMap()

        val results = fetchAllStoredCompanyFields(filteredAndSortedResults).sortedBy { sortingMap[it.companyId]!! }

        return results.map { it.toApiModel() }
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
    fun getCompanyApiModelById(companyId: String): StoredCompany {
        val searchResult = getCompanyById(companyId)
        return fetchAllStoredCompanyFields(listOf(searchResult)).first().toApiModel()
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
}
