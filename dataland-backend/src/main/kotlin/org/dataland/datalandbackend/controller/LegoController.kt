package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.LegoApi
import org.dataland.datalandbackend.services.LegoManager
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the company data endpoints
 * @param legoManager the manager for dealing with legos
 */

@RestController
class LegoController(
    @Autowired private val legoManager: LegoManager,
) : LegoApi {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun postLego(
        framework: String,
        reportingPeriod: String,
        companyId: String,
        data: String
    ): ResponseEntity<String> {
        logger.info("Received a request to post lego data for $framework $reportingPeriod $companyId")
        legoManager.storeData(framework, reportingPeriod, companyId, data)
        return ResponseEntity.ok("Successfully stored data.")
    }

    override fun getLego(framework: String, reportingPeriod: String, companyId: String): ResponseEntity<String> {
        logger.info("Received a request to get lego data for $framework $reportingPeriod $companyId")
        return ResponseEntity.ok(legoManager.retrieveData(framework, reportingPeriod, companyId))
    }
}
