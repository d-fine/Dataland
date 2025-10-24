package org.dataland.datalanduserservice.controller

import org.dataland.datalanduserservice.api.SubscribedCompaniesApi
import org.dataland.datalanduserservice.service.CompanyReportingInfoService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * RestController for the Statistics API
 */
@RestController
class SubscribedCompaniesController
    @Autowired
    constructor(
        private val companyReportingInfoService: CompanyReportingInfoService,
    ) : SubscribedCompaniesApi {
        override fun getCompaniesWithIncompleteFyeInformation(): ResponseEntity<Set<String>> =
            ResponseEntity
                .ok(companyReportingInfoService.getCachedCompanyIdsWithoutReportingYearInfo())
    }
