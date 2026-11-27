package org.dataland.datalanduserservice.controller

import org.dataland.datalanduserservice.api.SubscribedCompaniesApi
import org.dataland.datalanduserservice.service.IncompleteCompanyInformationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * RestController for the Subscribed Companies API
 */
@RestController
class SubscribedCompaniesController
    @Autowired
    constructor(
        private val incompleteCompanyInformationService: IncompleteCompanyInformationService,
    ) : SubscribedCompaniesApi {
        override fun getCompaniesWithIncompleteInformation(): ResponseEntity<Set<String>> =
            ResponseEntity.ok(incompleteCompanyInformationService.getIncompleteCompanyIds())

        override fun postCompanyWithIncompleteInformation(companyId: String): ResponseEntity<Unit> {
            incompleteCompanyInformationService.registerIncompleteCompany(companyId, UUID.randomUUID().toString())
            return ResponseEntity.ok().build()
        }
    }
