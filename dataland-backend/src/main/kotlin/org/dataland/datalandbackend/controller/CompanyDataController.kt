package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.CompanyAPI
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.model.CompanyMetaInformation
import org.dataland.datalandbackend.model.PostCompanyRequestBody
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

    override fun postCompany(postCompanyRequestBody: PostCompanyRequestBody): ResponseEntity<CompanyMetaInformation> {
        return ResponseEntity.ok(this.dataManager.addCompany(postCompanyRequestBody.companyName))
    }

    override fun getCompaniesByName(companyName: String?): ResponseEntity<List<CompanyMetaInformation>> {
        return ResponseEntity.ok(this.dataManager.listCompaniesByName(companyName ?: ""))
    }

    override fun getCompanyById(companyId: String): ResponseEntity<CompanyMetaInformation> {
        return ResponseEntity.ok(this.dataManager.getCompanyById(companyId))
    }
}
