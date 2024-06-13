package org.dataland.datalandcommunitymanager.controller
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandcommunitymanager.api.DataOwnerApi
import org.dataland.datalandcommunitymanager.model.dataOwner.CompanyDataOwners
import org.dataland.datalandcommunitymanager.services.DataOwnerManager
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Controller for the data ownernship endpoints
 * @param dataOwnersManager the service to handle data ownership operations
 * @param companyApi the service to communicate with the dataland backend microservice
 */

@RestController
class DataOwnerController(
    @Autowired private val dataOwnersManager: DataOwnerManager,
    @Autowired private val companyApi: CompanyDataControllerApi,
) : DataOwnerApi {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun postDataOwner(companyId: UUID, userId: UUID): ResponseEntity<CompanyDataOwners> {
        logger.info("Received a request to post a data owner with Id $userId to company with Id $companyId.")

        val storedCompany = companyApi.getCompanyById(companyId.toString())
        val companyName = storedCompany.companyInformation.companyName

        val companyDataOwnersEntity = dataOwnersManager.addDataOwnerToCompany(
            companyId.toString(),
            userId.toString(),
            companyName,
        )
        return ResponseEntity.ok(
            CompanyDataOwners(
                companyId = companyDataOwnersEntity.companyId,
                dataOwners = companyDataOwnersEntity.dataOwners,
            ),
        )
    }
    override fun getDataOwners(companyId: UUID): ResponseEntity<List<String>> {
        logger.info("Received a request to get a data owner from company Id $companyId.")
        val companyDataOwnersEntity = dataOwnersManager.getDataOwnerFromCompany(companyId.toString())
        return ResponseEntity.ok(companyDataOwnersEntity.dataOwners)
    }

    override fun deleteDataOwner(companyId: UUID, userId: UUID): ResponseEntity<CompanyDataOwners> {
        logger.info("Received a request to delete a data owner with Id $userId to company with Id $companyId.")
        val companyDataOwnersEntity = dataOwnersManager.deleteDataOwnerFromCompany(
            companyId.toString(),
            userId.toString(),
        )
        return ResponseEntity.ok(
            CompanyDataOwners(
                companyId = companyDataOwnersEntity.companyId,
                dataOwners = companyDataOwnersEntity.dataOwners,
            ),
        )
    }

    override fun isUserDataOwnerForCompany(companyId: UUID, userId: UUID) {
        logger.info("Received a request to check if user with Id $userId is data owner of company with Id $companyId.")
        dataOwnersManager.checkUserCompanyCombinationForDataOwnership(companyId.toString(), userId.toString())
    }

    override fun postDataOwnershipRequest(companyId: UUID, comment: String?) {
        val userAuthentication = DatalandAuthentication.fromContext()
        val correlationId = UUID.randomUUID().toString()
        logger.info(
            "User (id: ${userAuthentication.userId}) requested data ownership for company with id: $companyId. " +
                "(correlationId: $correlationId)",
        )
        dataOwnersManager.sendDataOwnershipRequestIfNecessary(
            companyId.toString(), userAuthentication,
            comment, correlationId,
        )
    }

    override fun hasCompanyDataOwner(companyId: UUID) {
        logger.info("Received a request to check if $companyId has data owner(s)")
        dataOwnersManager.checkCompanyForDataOwnership(companyId.toString())
    }
}
