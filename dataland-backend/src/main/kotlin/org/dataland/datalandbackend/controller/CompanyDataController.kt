package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.CompanyAPI
import org.dataland.datalandbackend.interfaces.DataStoreInterface
import org.dataland.datalandbackend.model.Company
import org.dataland.datalandbackend.model.CompanyMetaInformation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Implementation of the API for company data exchange
 * @param dataStore implementation of the DataStoreInterface that defines how uploaded company data is to be stored
 */

@RestController
class CompanyDataController(
    @Autowired @Qualifier("DefaultStore") var dataStore: DataStoreInterface,
) : CompanyAPI {

    override fun getAllCompanies(): ResponseEntity<Map<String, Company>> {
        return ResponseEntity.ok(this.dataStore.listAllCompanies())
    }

    override fun postCompany(companyName: String): ResponseEntity<CompanyMetaInformation> {
        return ResponseEntity.ok(this.dataStore.addCompany(companyName))
    }

    override fun getCompanyByName(companyName: String): ResponseEntity<Map<String, Company>> {
        return ResponseEntity.ok(this.dataStore.listCompaniesByName(companyName))
    }
}
