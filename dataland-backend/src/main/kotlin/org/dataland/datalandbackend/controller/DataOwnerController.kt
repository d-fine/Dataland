package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.DataOwnerApi
import org.dataland.datalandbackend.model.companies.CompanyDataOwners
import org.dataland.datalandbackend.services.DataOwnersManager
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the (company) data ownership endpoints
 * @param dataOwnersManager the manager to handle and edit data ownership relations
 */
@RestController
class DataOwnerController(
    @Autowired private val dataOwnersManager: DataOwnersManager,
) : DataOwnerApi {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun postDataOwner(companyId: String, userId: String): ResponseEntity<CompanyDataOwners> {
        logger.info("Received a request to post a data owner with Id $userId to company with Id $companyId.")
        val companyDataOwnersEntity = dataOwnersManager.addDataOwnerToCompany(companyId, userId)
        return ResponseEntity.ok(
            CompanyDataOwners(
                companyId = companyDataOwnersEntity.companyId,
                dataOwners = companyDataOwnersEntity.dataOwners,
            ),
        )
    }

    override fun deleteDataOwner(companyId: String, userId: String): ResponseEntity<CompanyDataOwners> {
        logger.info("Received a request to delete a data owner with Id $userId to company with Id $companyId.")
        val companyDataOwnersEntity = dataOwnersManager.deleteDataOwnerFromCompany(companyId, userId)
        return ResponseEntity.ok(
            CompanyDataOwners(
                companyId = companyDataOwnersEntity.companyId,
                dataOwners = companyDataOwnersEntity.dataOwners,
            ),
        )
    }
}
