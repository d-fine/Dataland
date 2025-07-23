package org.dataland.datalandcommunitymanager.controller

import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.validateIsEmailAddress
import org.dataland.datalandcommunitymanager.api.CompanyRolesApi
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRoleAssignment
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRolePost
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

    private fun isExactlyOneSpecified(
        userId: UUID?,
        email: String?,
    ): Boolean {
        if (userId == null && email == null) return false
        return userId == null || email == null
    }

    override fun assignCompanyRole(companyRolePost: CompanyRolePost): ResponseEntity<CompanyRoleAssignment> {
        companyRolePost.email?.validateIsEmailAddress()
        if (!isExactlyOneSpecified(companyRolePost.userId, companyRolePost.email)) {
            throw InvalidInputApiException(
                summary = "Invalid input regarding userId and email.",
                message = "Please specify exactly one of the parameters 'userId' and 'email'.",
            )
        }
        logger.info(
            "Received a request to assign the company role ${companyRolePost.companyRole} " +
                "for company ${companyRolePost.companyId} to the user " +
                if (companyRolePost.userId != null) "${companyRolePost.userId}" else "with email ${companyRolePost.email}",
        )
        return ResponseEntity.ok(
            companyRolesManager
                .assignCompanyRoleForCompanyToUser(
                    companyRolePost.companyRole,
                    companyRolePost.companyId.toString(),
                    companyRolePost.userId?.toString() ?: companyRolePost.email!!,
                    companyRolePost.userId != null,
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
        val entities =
            companyRolesManager
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

    override fun hasUserCompanyRole(
        companyRole: CompanyRole,
        companyId: UUID,
        userId: UUID,
    ) {
        logger.info(
            "Received a request to check if user with Id $userId has the role $companyRole for the company $companyId",
        )
        companyRolesManager.validateIfCompanyRoleForCompanyIsAssignedToUser(
            companyRole,
            companyId.toString(),
            userId.toString(),
        )
    }

    override fun postCompanyOwnershipRequest(
        companyId: UUID,
        comment: String?,
    ) {
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
