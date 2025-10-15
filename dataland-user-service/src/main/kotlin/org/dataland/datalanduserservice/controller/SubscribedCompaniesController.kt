package org.dataland.datalanduserservice.controller

import org.dataland.datalanduserservice.api.SubscribedCompaniesApi
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * RestController for the Statistics API
 */
@RestController
class SubscribedCompaniesController : SubscribedCompaniesApi {
    override fun getCompaniesWithIncompleteFyeInformation(): ResponseEntity<List<String>> {
        // Dummy data for now
        val companyIds = listOf("COMPANY123", "COMPANY456", "COMPANY789")
        return ResponseEntity.ok(companyIds)
    }
}
