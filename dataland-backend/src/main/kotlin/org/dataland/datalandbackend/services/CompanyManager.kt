package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyIdentifierEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.interfaces.CompanyManagerInterface
import org.dataland.datalandbackend.model.*
import org.dataland.datalandbackend.model.enums.company.StockIndex
import org.dataland.datalandbackend.repositories.CompanyIdentifierRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
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

    override fun getTeaserCompanyIds(): List<String> {
        return listOf()
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
        var companies = companyRepository.findAll()

        companies = if (onlyCompanyNames) {
            filterCompaniesByNameOnly(searchString, companies)
        } else {
            filterCompaniesByNameAndIdentifier(searchString, companies)
        }

        companies = filterCompaniesByStockIndices(stockIndexFilter, companies)
        companies = filterCompaniesByDataTypes(dataTypeFilter, companies)

        return companies
    }

    private fun filterCompaniesByNameAndIdentifier(
        searchString: String,
        companies: List<StoredCompanyEntity>
    ): List<StoredCompanyEntity> {
        if (searchString == "") return companies
        return (
            filterCompaniesByNameOnly(searchString, companies) +
                filterCompaniesByIdentifier(searchString, companies)
            ).distinct()
    }
    private fun filterCompaniesByNameOnly(name: String, companies: List<StoredCompanyEntity>): List<StoredCompanyEntity> {
        return (
            companies.filter { it.companyName.startsWith(name, true) } +
                companies.filter { it.companyName.contains(name, true) }
            ).distinct()
    }

    private fun filterCompaniesByIdentifier(searchString: String, companies: List<StoredCompanyEntity>): List<StoredCompanyEntity> {
        if (searchString == "") return companies
        return companies.filter {
            it.identifiers.any { identifier ->
                identifier.identifierValue.contains(searchString, true)
            }
        }
    }

    private fun filterCompaniesByStockIndices(
        indices: Set<StockIndex>,
        companies: List<StoredCompanyEntity>
    ): List<StoredCompanyEntity> {
        if (indices.isEmpty()) return companies
        return companies.filter {
            it.indices.any { index -> indices.contains(index) }
        }
    }

    private fun filterCompaniesByDataTypes(
        dataTypes: Set<DataType>,
        companies: List<StoredCompanyEntity>
    ): List<StoredCompanyEntity> {
        if (dataTypes.isEmpty()) return companies
        return companies.map {
                company ->
            company.copy(
                dataRegisteredByDataland = company.dataRegisteredByDataland.filter
                { dataTypes.contains(DataType.valueOf(it.dataType)) }.toMutableList()
            )
        }.filter {
            it.dataRegisteredByDataland.isNotEmpty()
        }
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

    override fun isCompanyPublic(companyId: String): Boolean {
        return false
    }
}
