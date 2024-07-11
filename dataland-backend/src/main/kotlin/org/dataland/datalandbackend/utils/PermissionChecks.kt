package org.dataland.datalandbackend.utils

import org.dataland.datalandbackend.services.CompanyRoleChecker
import org.dataland.datalandcommunitymanager.openApiClient.model.CompanyRole
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PermissionChecks(
    @Autowired private val companyRoleChecker: CompanyRoleChecker,
) {
    /**
     * A user can bypass QA if
     * (a) the user has the uploader and QA role
     * (b) the user is an admin
     * (c) the user owns the company
     * This function checks these conditions
     */
    fun canUserBypassQa(viewingUser: DatalandAuthentication?, companyId: String): Boolean {
        return (
            viewingUser?.roles?.contains(DatalandRealmRole.ROLE_UPLOADER) ?: false &&
                viewingUser?.roles?.contains(DatalandRealmRole.ROLE_REVIEWER) ?: false
            ) ||
            (viewingUser?.roles?.contains(DatalandRealmRole.ROLE_ADMIN) ?: false) ||
            (
                viewingUser?.roles?.contains(DatalandRealmRole.ROLE_USER) ?: false &&
                    companyRoleChecker.hasCurrentUserGivenRoleForCompany(companyId, CompanyRole.CompanyOwner)
                )
    }
}
