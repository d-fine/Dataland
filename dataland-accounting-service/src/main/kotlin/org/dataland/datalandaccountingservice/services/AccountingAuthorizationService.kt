package org.dataland.datalandaccountingservice.services

import org.dataland.datalandbackendutils.model.InheritedRole
import org.dataland.datalandcommunitymanager.openApiClient.api.InheritedRolesControllerApi
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.stereotype.Service

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

    fun hasUserRoleInMemberCompany(companyId: String): Boolean {
        val authentication =
            try {
                DatalandAuthentication.fromContext()
            } catch (ex: IllegalArgumentException) {
                throw InsufficientAuthenticationException("No authentication found in context", ex)
            }
        val userId = authentication.userId
        if (userId.isBlank()) {
            throw InsufficientAuthenticationException("userId is blank or missing in authentication.")
        }
        val inheritedRoles = inheritedRolesControllerApi.getInheritedRoles(userId)
        return inheritedRoles[companyId]?.contains(InheritedRole.DatalandMember.name) == true
    }
}
