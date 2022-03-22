package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.CompanyAPI
import org.dataland.datalandbackend.interfaces.DataStoreInterface
import org.dataland.datalandbackend.model.CompanyMetaInformation
import org.dataland.datalandbackend.model.DataIdentifier
import org.dataland.datalandbackend.model.PostCompanyRequestBody
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Implementation of the API for company data exchange
 * @param dataStore implementation of the DataStoreInterface that defines how uploaded company data is to be stored
 */

@RestController
class CompanyDataController(
    @Autowired var dataStore: DataStoreInterface,
) : CompanyAPI {

    override fun postCompany(postCompanyRequestBody: PostCompanyRequestBody): ResponseEntity<CompanyMetaInformation> {
        return ResponseEntity.ok(this.dataStore.addCompany(postCompanyRequestBody.companyName))
    }

    override fun getCompaniesByName(companyName: String?): ResponseEntity<List<CompanyMetaInformation>> {
        return ResponseEntity.ok(this.dataStore.listCompaniesByName(companyName ?: ""))
    }

    override fun getCompanyDataSets(companyId: String): ResponseEntity<List<DataIdentifier>> {
        return ResponseEntity.ok(this.dataStore.listDataSetsByCompanyId(companyId))
    }

    override fun getCompanyById(companyId: String): ResponseEntity<CompanyMetaInformation> {
        return ResponseEntity.ok(this.dataStore.getCompanyById(companyId))
    }
}
