package org.dataland.datalandaccountingservice.services

import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.stereotype.Service

/**
 * Helper service class to handle authorization related to accounting.
 */
@Service("AccountingAuthorizationService")
class AccountingAuthorizationService(
    private val companyRolesControllerApi: CompanyRolesControllerApi,
) {
    /**
     * Check whether the currently authenticated user has any role in the specified company.
     */
    fun hasUserSomeRoleInCompany(companyId: String): Boolean {
        val userId = DatalandAuthentication.fromContext().userId
        return companyRolesControllerApi
            .getExtendedCompanyRoleAssignments(
                role = null,
                companyId = ValidationUtils.convertToUUID(companyId),
                userId = ValidationUtils.convertToUUID(userId),
            ).isNotEmpty()
    }
}
