package org.dataland.datalandbackend.service

import org.dataland.datalandbackend.interfaces.CompanyDataStoreInterface
import org.dataland.datalandbackend.model.CompanyMetaInformation
import org.springframework.stereotype.Component

/**
 * Implementation of a company data manager for Dataland including metadata storages
 */
@Component
class CompanyDataManager : CompanyDataStoreInterface, DataManager() {

    override fun addCompany(companyName: String): CompanyMetaInformation {
        companyCounter++
        companyData["$companyCounter"] = CompanyMetaInformation(
            companyId = companyCounter.toString(),
            companyName = companyName,
            dataRegisteredByDataland = mutableListOf()
        )
        return companyData["$companyCounter"]!!
    }

    override fun listCompaniesByName(companyName: String): List<CompanyMetaInformation> {
        return companyData.filter { it.value.companyName.contains(companyName, true) }.map {
            CompanyMetaInformation(
                companyId = it.key,
                companyName = it.value.companyName,
                dataRegisteredByDataland = it.value.dataRegisteredByDataland
            )
        }
    }

    override fun getCompanyById(companyId: String): CompanyMetaInformation {
        verifyCompanyIdExists(companyId)

        return companyData[companyId]!!
    }
}
