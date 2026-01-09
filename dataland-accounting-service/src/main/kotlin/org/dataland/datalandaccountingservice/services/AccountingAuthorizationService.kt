package org.dataland.datalandaccountingservice.services

import org.dataland.datalandbackendutils.model.InheritedRole
import org.dataland.datalandcommunitymanager.openApiClient.api.InheritedRolesControllerApi
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
/**
 * Helper service class to handle authorization related to accounting.
 */
@Service("AccountingAuthorizationService")
class AccountingAuthorizationService(
    private val inheritedRolesControllerApi: InheritedRolesControllerApi,
) {
    /**
     * Check whether the currently authenticated user has any role in the specified company.
     */
    private val log = LoggerFactory.getLogger(AccountingAuthorizationService::class.java)

    fun hasUserRoleInMemberCompany(companyId: String): Boolean {
        val userId =
            try {
                DatalandAuthentication.fromContext().userId
            } catch (_: IllegalArgumentException) {
                null
            }

        if (userId.isNullOrBlank()) { return false}



        val inheritedRoles = inheritedRolesControllerApi.getInheritedRoles(userId)
        val rolesForCompany = inheritedRoles[companyId]

        val hasRole = rolesForCompany?.isEmpty() != true

        log.info("has User Role in Member Company: userId= {}, companyId = {}, rolesForCompany = {}, hasRole = {}", userId, companyId, rolesForCompany, hasRole)

        return hasRole
    }
}
