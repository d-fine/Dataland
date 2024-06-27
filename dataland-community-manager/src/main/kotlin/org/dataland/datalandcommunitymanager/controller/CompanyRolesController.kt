package org.dataland.datalandcommunitymanager.controller
import org.dataland.datalandcommunitymanager.api.CompanyRolesApi
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRoleAssignment
import org.dataland.datalandcommunitymanager.services.CompanyRolesManager
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Controller for the company roles endpoints
 * @param companyRolesManager the service to handle company roles operations
 */

@RestController
class CompanyRolesController(
    @Autowired private val companyRolesManager: CompanyRolesManager,
) : CompanyRolesApi {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun assignCompanyRole(
        companyRole: CompanyRole,
        companyId: UUID,
        userId: UUID,
    ): ResponseEntity<CompanyRoleAssignment> {
        logger.info(
            "Received a request to assign the company role $companyRole for company $companyId to the user $userId",
        )
        return ResponseEntity.ok(
            companyRolesManager.assignCompanyRoleForCompanyToUser(
                companyRole,
                companyId.toString(),
                userId.toString(),
            ).toApiModel(),
        )
    }
    override fun getCompanyRoleAssignments(
        companyRole: CompanyRole?,
        companyId: UUID?,
        userId: UUID?,
    ): ResponseEntity<List<CompanyRoleAssignment>> {
        logger.info(
            "Received a request to get company role assignments for company role $companyRole for company $companyId",
        )
        val entities = companyRolesManager
            .getCompanyRoleAssignmentsByParameters(companyRole, companyId?.toString(), userId?.toString())
        return ResponseEntity.ok(
            entities.map { it.toApiModel() },
        )
    }

    override fun removeCompanyRole(
        companyRole: CompanyRole,
        companyId: UUID,
        userId: UUID,
    ) {
        logger.info(
            "Received a request to remove the company role $companyRole for company $companyId from the user $userId",
        )
        companyRolesManager.removeCompanyRoleForCompanyFromUser(companyRole, companyId.toString(), userId.toString())
    }

    override fun hasUserCompanyRole(companyRole: CompanyRole, companyId: UUID, userId: UUID) {
        logger.info(
            "Received a request to check if user with Id $userId has the role $companyRole for the company $companyId",
        )
        companyRolesManager.validateIfCompanyRoleForCompanyIsAssignedToUser(
            companyRole,
            companyId.toString(),
            userId.toString(),
        )
    }

    override fun postCompanyOwnershipRequest(companyId: UUID, comment: String?) {
        val userAuthentication = DatalandAuthentication.fromContext()
        val correlationId = UUID.randomUUID().toString()
        logger.info(
            "User (id: ${userAuthentication.userId}) requested company ownership for company with id: $companyId " +
                "(correlationId: $correlationId)",
        )
        companyRolesManager.triggerCompanyOwnershipRequest(
            companyId.toString(), userAuthentication,
            comment, correlationId,
        )
    }

    override fun hasCompanyAtLeastOneOwner(companyId: UUID) {
        logger.info("Received a request to check if $companyId has company owner(s)")
        companyRolesManager.validateIfCompanyHasAtLeastOneCompanyOwner(companyId.toString())
    }
}
