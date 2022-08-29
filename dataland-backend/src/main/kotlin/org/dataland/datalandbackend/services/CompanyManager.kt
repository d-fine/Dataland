package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyIdentifierEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.interfaces.CompanyManagerInterface
import org.dataland.datalandbackend.model.*
import org.dataland.datalandbackend.model.enums.company.StockIndex
import org.dataland.datalandbackend.repositories.CompanyIdentifierRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackend.repositories.utils.StoredCompanySearchFilter
import org.hibernate.exception.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.transaction.Transactional

/**
 * Implementation of a company manager for Dataland
 */
@Component("CompanyManager")
class CompanyManager(
    @Autowired private val companyRepository: StoredCompanyRepository,
    @Autowired private val companyIdentifierRepository: CompanyIdentifierRepository
) : CompanyManagerInterface {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun verifyCompanyIdExists(companyId: String) {
        if (!companyRepository.existsById(companyId)) {
            throw IllegalArgumentException("Dataland does not know the company ID $companyId")
        }
    }

    private fun createStoredCompanyEntityWithoutIdentifiers(
        companyId: String,
        companyInformation: CompanyInformation
    ) : StoredCompanyEntity {
        val newCompanyEntity = StoredCompanyEntity(
            companyId = companyId,
            companyName = companyInformation.companyName,
            headquarters = companyInformation.headquarters,
            sector = companyInformation.sector,
            marketCap = companyInformation.marketCap,
            reportingDateOfMarketCap = companyInformation.reportingDateOfMarketCap,
            indices = companyInformation.indices,
            countryCode = companyInformation.countryCode,
            identifiers = mutableListOf(),
            dataRegisteredByDataland = mutableListOf()
        )

        val savedCompanyEntity = companyRepository.save(newCompanyEntity)
        return savedCompanyEntity
    }

    private fun createAndAssociateIdentifiers(savedCompanyEntity: StoredCompanyEntity,
                                              companyInformation: CompanyInformation
    ): List<CompanyIdentifierEntity> {
        val newIdentifiers = companyInformation.identifiers.map {
            CompanyIdentifierEntity(
                identifierType = it.identifierType,
                identifierValue = it.identifierValue,
                company = savedCompanyEntity,
                isNew = true,
            )
        }

        try {
            return companyIdentifierRepository.saveAllAndFlush(newIdentifiers).toList()
        } catch (ex : DataIntegrityViolationException) {
            val cause = ex.cause
            if (cause is ConstraintViolationException && cause.constraintName == "company_identifiers_pkey") {
                throw IllegalArgumentException("Could not insert company as one company identifier is already used to identify another company")
            }
            throw ex
        }
    }

    @Transactional
    override fun addCompany(companyInformation: CompanyInformation): StoredCompanyEntity {
        val companyId = UUID.randomUUID().toString()
        logger.info("Creating Company ${companyInformation.companyName} with ID $companyId")
        val savedCompany = createStoredCompanyEntityWithoutIdentifiers(companyId, companyInformation)
        val identifiers = createAndAssociateIdentifiers(savedCompany, companyInformation)
        savedCompany.identifiers = identifiers.toMutableList()

        return savedCompany
    }

    override fun searchCompanies(
        searchString: String,
        onlyCompanyNames: Boolean,
        dataTypeFilter: Set<DataType>,
        stockIndexFilter: Set<StockIndex>
    ): List<StoredCompanyEntity> {
        var searchFilter = StoredCompanySearchFilter(
            searchString = searchString,
            nameOnlyFilter = onlyCompanyNames,
            dataTypeFilter = dataTypeFilter.map { it.name },
            stockIndexFilter = stockIndexFilter.toList(),
        )
        return companyRepository.searchCompanies(searchFilter)
    }

    override fun getCompanyById(companyId: String): StoredCompanyEntity {
        val storedCompanySearchResult = companyRepository.findById(companyId)
        if (storedCompanySearchResult.isEmpty) {
            throw IllegalArgumentException("Dataland does not know the company ID $companyId")
        }
        return storedCompanySearchResult.get()
    }

    override fun setTeaserCompanies(companyIds: List<String>) {

    }

    override fun getTeaserCompanyIds(): List<String> {
        return listOf()
    }
    override fun isCompanyPublic(companyId: String): Boolean {
        return false
    }
}
