package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.CompanyAPI
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.model.CompaniesRequestBody
import org.dataland.datalandbackend.model.CompanyMetaInformation
import org.dataland.datalandbackend.model.DataIdentifier
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Implementation of the API for company data exchange
 * @param dataManager implementation of the DataManagerInterface that defines how
 * Dataland handles data
 */

@RestController
class CompanyDataController(
    @Autowired @Qualifier("DefaultManager") var dataManager: DataManagerInterface,
) : CompanyAPI {

    override fun postCompany(companyName: CompaniesRequestBody): ResponseEntity<CompanyMetaInformation> {
        return ResponseEntity.ok(this.dataManager.addCompany(companyName.companyName))
    }

    override fun getCompaniesByName(companyName: String?): ResponseEntity<List<CompanyMetaInformation>> {
        return ResponseEntity.ok(this.dataManager.listCompaniesByName(companyName ?: ""))
    }

    override fun getCompanyDataSets(companyId: String): ResponseEntity<List<DataIdentifier>> {
        return ResponseEntity.ok(this.dataManager.listDataSetsByCompanyId(companyId))
    }

    override fun getCompanyById(companyId: String): ResponseEntity<CompanyMetaInformation> {
        return ResponseEntity.ok(this.dataManager.getCompanyById(companyId))
    }
}
