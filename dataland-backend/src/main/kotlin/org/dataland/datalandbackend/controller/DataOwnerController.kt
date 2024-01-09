package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.DataOwnerApi
import org.dataland.datalandbackend.model.companies.CompanyDataOwners
import org.dataland.datalandbackend.services.DataOwnersManager
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Controller for the (company) data ownership endpoints
 * @param dataOwnersManager the manager to handle and edit data ownership relations
 */
@RestController
class DataOwnerController(
    @Autowired private val dataOwnersManager: DataOwnersManager,
) : DataOwnerApi {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun postDataOwner(companyId: UUID, userId: UUID): ResponseEntity<CompanyDataOwners> {
        logger.info("Received a request to post a data owner with Id $userId to company with Id $companyId.")
        val companyDataOwnersEntity = dataOwnersManager.addDataOwnerToCompany(companyId.toString(), userId.toString())
        return ResponseEntity.ok(
            CompanyDataOwners(
                companyId = companyDataOwnersEntity.companyId,
                dataOwners = companyDataOwnersEntity.dataOwners,
            ),
        )
    }
    override fun getDataOwners(companyId: UUID): ResponseEntity<List<String>> {
        val companyDataOwnersEntity = dataOwnersManager.getDataOwnerFromCompany(companyId.toString())
        return ResponseEntity.ok(companyDataOwnersEntity.dataOwners)
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

    override fun isUserDataOwnerForCompany(companyId: UUID, userId: UUID) {
        logger.info("Received a request to check if user with Id $userId is data owner of company with Id $companyId.")
        dataOwnersManager.checkUserCompanyCombinationForDataOwnership(companyId, userId)
    }
}
