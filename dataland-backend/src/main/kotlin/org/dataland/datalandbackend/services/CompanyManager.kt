package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyIdentifierEntity
import org.dataland.datalandbackend.entities.CompanyIdentifierEntityId
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.model.CompanyIdAndName
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.CompanySearchFilter
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.CompanyIdentifierRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackend.repositories.utils.StoredCompanySearchFilter
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.hibernate.exception.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Pageable
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
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
        companyInformation: CompanyInformation,
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
            isTeaserCompany = companyInformation.isTeaserCompany,
            website = companyInformation.website,
        )

        return companyRepository.save(newCompanyEntity)
    }

    private fun createAndAssociateIdentifiers(
        savedCompanyEntity: StoredCompanyEntity,
        companyInformation: CompanyInformation,
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
                    "Could not insert company as one company identifier is already used to identify another company",
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
    @Transactional(rollbackFor = [InvalidInputApiException::class])
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
     * @param filter The filter to use during searching
     * @param viewingUser The user that is viewing the API model
     * @return list of all matching companies in Dataland
     */
    @Transactional
    fun searchCompaniesAndGetApiModel(
        filter: CompanySearchFilter,
        viewingUser: DatalandAuthentication? = null,
    ): List<StoredCompany> {
        if (filter.dataTypeFilter.isEmpty()) {
            throw InvalidInputApiException(
                "Requestparam has a non acceptable value",
                "Please specify a dataframework",
            )
        }
        val searchFilterForJPA = StoredCompanySearchFilter(
            searchString = filter.searchString,
            nameOnlyFilter = filter.onlyCompanyNames,
            dataTypeFilter = filter.dataTypeFilter.map { it.name },
            sectorFilter = filter.sectorFilter.toList(),
            countryCodeFilter = filter.countryCodeFilter.toList(),
            uploaderIdFilter = getUploaderIdFilter(filter.onlyCurrentUserAsUploader),
        )

        val filteredAndSortedResults = companyRepository.searchCompanies(
            searchFilterForJPA,
            Pageable.unpaged(),
        )

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

    private fun getUploaderIdFilter(onlyCurrentUserAsUploader: Boolean): List<String> {
        return if (onlyCurrentUserAsUploader) {
            listOf(DatalandAuthentication.fromContext().userId)
        } else {
            listOf()
        }
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
    @Transactional

    fun isCompanyPublic(companyId: String): Boolean {
        return getCompanyById(companyId).isTeaserCompany
    }

    /**
     * Checks if an identifier already exists, if not an exception is thrown
     * @param identifierType type of the identifier to check
     * @param identifier identifier to check
     */
    fun checkIfIdentifierExists(identifierType: IdentifierType, identifier: String) {
        try {
            companyIdentifierRepository.getReferenceById(CompanyIdentifierEntityId(identifier, identifierType))
        } catch (e: JpaObjectRetrievalFailureException) {
            throw ResourceNotFoundApiException(
                "Company identifier does not exist",
                "Company identifier $identifier of type $identifierType does not exist",
                e
            )
        }
    }
}
