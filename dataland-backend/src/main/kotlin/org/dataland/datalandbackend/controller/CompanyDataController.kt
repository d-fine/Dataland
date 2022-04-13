package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.CompanyAPI
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.StoredCompany
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Implementation of the API for company data exchange
 * @param dataManager implementation of the DataManagerInterface that defines how
 * Dataland handles data
 */

@RestController
class CompanyDataController(
    @Autowired var dataManager: DataManagerInterface,
) : CompanyAPI {

    override fun postCompany(companyInformation: CompanyInformation): ResponseEntity<StoredCompany> {
        return ResponseEntity.ok(dataManager.addCompany(companyInformation))
    }

    override fun getCompanies(
        searchString: String?,
        selectedIndex: CompanyInformation.StockIndex?,
        onlyCompanyNames: Boolean
    ): ResponseEntity<List<StoredCompany>> {
        return if (selectedIndex == null) {
            ResponseEntity.ok(dataManager.searchCompanies(searchString ?: "", onlyCompanyNames))
        } else {
            ResponseEntity.ok(dataManager.searchCompaniesByIndex(selectedIndex))
        }
    }

    override fun getCompanyById(companyId: String): ResponseEntity<StoredCompany> {
        return ResponseEntity.ok(dataManager.getCompanyById(companyId))
    }
}
