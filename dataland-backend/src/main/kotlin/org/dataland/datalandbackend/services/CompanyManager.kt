package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.interfaces.CompanyManagerInterface
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StoredCompany
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.Collections
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of a company manager for Dataland
 */
@Component("CompanyManager")
class CompanyManager : CompanyManagerInterface {
    var companyDataPerCompanyId = ConcurrentHashMap<String, StoredCompany>()
    private var teaserCompanyIds: List<String> = listOf<String>()
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun verifyCompanyIdExists(companyId: String) {
        if (!companyDataPerCompanyId.containsKey(companyId)) {
            throw IllegalArgumentException("Dataland does not know the company ID $companyId")
        }
    }

    override fun addMetaDataInformationToCompanyStore(companyId: String, dataMetaInformation: DataMetaInformation) {
        companyDataPerCompanyId[companyId]!!.dataRegisteredByDataland.add(dataMetaInformation)
    }

    override fun getTeaserCompanyIds(): List<String> {
        return teaserCompanyIds
    }

    override fun addCompany(companyInformation: CompanyInformation): StoredCompany {
        val companyId = UUID.randomUUID().toString()
        logger.info("Adding Company ${companyInformation.companyName} with ID $companyId")
        companyDataPerCompanyId[companyId] = StoredCompany(
            companyId = companyId,
            companyInformation,
            dataRegisteredByDataland = Collections.synchronizedList(mutableListOf())
        )
        return companyDataPerCompanyId[companyId]!!
    }

    override fun searchCompanies(
        searchString: String,
        onlyCompanyNames: Boolean,
        dataTypeFilter: Set<DataType>
    ): List<StoredCompany> {
        var companies = companyDataPerCompanyId.values.toList()

        companies = if (onlyCompanyNames) {
            filterCompaniesByNameOnly(searchString, companies)
        } else {
            filterCompaniesByNameAndIdentifier(searchString, companies)
        }

        companies = filterCompaniesByDataTypes(dataTypeFilter, companies)

        return companies
    }

    private fun filterCompaniesByNameAndIdentifier(
        searchString: String,
        companies: List<StoredCompany>
    ): List<StoredCompany> {
        if (searchString == "") return companies
        return (
            filterCompaniesByNameOnly(searchString, companies) +
                filterCompaniesByIdentifier(searchString, companies)
            ).distinct()
    }
    private fun filterCompaniesByNameOnly(name: String, companies: List<StoredCompany>): List<StoredCompany> {
        return (
            companies.filter { it.companyInformation.companyName.startsWith(name, true) } +
                companies.filter { it.companyInformation.companyName.contains(name, true) }
            ).distinct()
    }

    private fun filterCompaniesByIdentifier(searchString: String, companies: List<StoredCompany>): List<StoredCompany> {
        if (searchString == "") return companies
        return companies.filter {
            it.companyInformation.identifiers.any { identifier ->
                identifier.identifierValue.contains(searchString, true)
            }
        }
    }

    private fun filterCompaniesByDataTypes(
        dataTypes: Set<DataType>,
        companies: List<StoredCompany>
    ): List<StoredCompany> {
        if (dataTypes.isEmpty()) return companies
        return companies.map {
                company ->
            company.copy(
                dataRegisteredByDataland = company.dataRegisteredByDataland.filter
                { dataTypes.contains(it.dataType) }.toMutableList()
            )
        }.filter {
            it.dataRegisteredByDataland.isNotEmpty()
        }
    }

    override fun getCompanyById(companyId: String): StoredCompany {
        verifyCompanyIdExists(companyId)
        return companyDataPerCompanyId[companyId]!!
    }

    override fun setTeaserCompanies(companyIds: List<String>) {
        logger.info("Setting Teaser Company IDs: $companyIds")
        teaserCompanyIds = companyIds
    }

    override fun isCompanyPublic(companyId: String): Boolean {
        return teaserCompanyIds.contains(companyId)
    }
}
