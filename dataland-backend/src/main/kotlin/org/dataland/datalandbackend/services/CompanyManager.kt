package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyIdentifierEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.exceptions.InvalidInputApiException
import org.dataland.datalandbackend.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackend.interfaces.CompanyManagerInterface
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.repositories.CompanyIdentifierRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackend.repositories.utils.StoredCompanySearchFilter
import org.hibernate.exception.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
import java.util.UUID
import javax.transaction.Transactional

/**
 * Implementation of a company manager for Dataland
 */
@Component("CompanyManager")
class CompanyManager(
    @Autowired private val companyRepository: StoredCompanyRepository,
    @Autowired private val companyIdentifierRepository: CompanyIdentifierRepository,
) : CompanyManagerInterface {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun verifyCompanyIdExists(companyId: String) {
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

    @Transactional
    override fun addCompany(companyInformation: CompanyInformation): StoredCompanyEntity {
        val companyId = UUID.randomUUID().toString()
        logger.info("Creating Company ${companyInformation.companyName} with ID $companyId")
        val savedCompany = createStoredCompanyEntityWithoutForeignReferences(companyId, companyInformation)
        val identifiers = createAndAssociateIdentifiers(savedCompany, companyInformation)
        savedCompany.identifiers = identifiers.toMutableList()
        return savedCompany
    }

    override fun searchCompanies(
        searchString: String,
        onlyCompanyNames: Boolean,
        dataTypeFilter: Set<DataType>,
        countryCodeFilter: Set<String>,
        sectorFilter: Set<String>,
    ): List<StoredCompanyEntity> {
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

        var filteredResults = companyRepository.fetchIdentifiers(filteredAndSortedResults)
        filteredResults = companyRepository.fetchAlternativeNames(filteredResults)
        filteredResults = companyRepository.fetchCompanyAssociatedByDataland(filteredResults)
        filteredResults = filteredResults.sortedBy { sortingMap[it.companyId]!! }

        return filteredResults
    }

    override fun getDistinctCountryCodes(): Set<String> {
        return companyRepository.fetchDistinctCountryCodes()
    }

    override fun getDistinctSectors(): Set<String> {
        return companyRepository.fetchDistinctSectors()
    }

    override fun getCompanyById(companyId: String): StoredCompanyEntity {
        val storedCompanySearchResult = companyRepository.findById(companyId)
        if (storedCompanySearchResult.isEmpty) {
            throw ResourceNotFoundApiException("Company not found", "Dataland does not know the company ID $companyId")
        }
        return storedCompanySearchResult.get()
    }

    override fun getTeaserCompanyIds(): List<String> {
        return companyRepository.getAllByIsTeaserCompanyIsTrue().map { it.companyId }
    }
    override fun isCompanyPublic(companyId: String): Boolean {
        return getCompanyById(companyId).isTeaserCompany
    }
}
