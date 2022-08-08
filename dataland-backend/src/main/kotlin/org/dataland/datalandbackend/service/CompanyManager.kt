package org.dataland.datalandbackend.service

import org.dataland.datalandbackend.interfaces.CompanyManagerInterface
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.model.enums.StockIndex
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
        companyDataPerCompanyId[companyId] = StoredCompany(
            companyId = companyId,
            companyInformation,
            dataRegisteredByDataland = Collections.synchronizedList(mutableListOf())
        )
        return companyDataPerCompanyId[companyId]!!
    }

    override fun searchCompanies(searchString: String, onlyCompanyNames: Boolean): List<StoredCompany> {
        val allCompanies = companyDataPerCompanyId.values.toList()

        return if (searchString == "") {
            allCompanies
        } else if (onlyCompanyNames) {
            filterCompaniesByName(searchString, allCompanies)
        } else {
            (
                filterCompaniesByName(searchString, allCompanies) +
                    filterCompaniesByIdentifier(searchString, allCompanies)
                ).distinct()
        }
    }

    private fun filterCompaniesByName(searchString: String, companies: List<StoredCompany>): List<StoredCompany> {
        return (
            companies.filter { it.companyInformation.companyName.startsWith(searchString, true) } +
                companies.filter { it.companyInformation.companyName.contains(searchString, true) }
            ).distinct()
    }

    private fun filterCompaniesByIdentifier(searchString: String, companies: List<StoredCompany>): List<StoredCompany> {
        return companies.filter {
            it.companyInformation.identifiers.any { identifier ->
                identifier.identifierValue.contains(searchString, true)
            }
        }
    }

    override fun searchCompaniesByIndex(selectedIndex: StockIndex): List<StoredCompany> {
        return companyDataPerCompanyId.values.filter {
            it.companyInformation.indices.any { index -> index == selectedIndex }
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
