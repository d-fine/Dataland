package org.dataland.datalandbackend.utils

import org.dataland.datalandbackend.services.CompanyRoleChecker
import org.dataland.datalandcommunitymanager.openApiClient.model.CompanyRole
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * A user can bypass QA if
 * (a) the user has the uploader and QA role
 * (b) the user is an admin
 * This function checks these conditions
 */
@Component
class QaPermissionChecks(
    @Autowired private val companyRoleChecker: CompanyRoleChecker,
) {
    /**
     * Checks whether the qa process should be bypassed for the current user.
     * @param viewingUser the user attempting to post the data set
     * @param companyId the company for which the data set is being posted
     * @return a Boolean indicating whether the QA process can be bypassed
     */
    fun canUserBypassQa(viewingUser: DatalandAuthentication?, companyId: String): Boolean {
        return (
            viewingUser?.roles?.contains(DatalandRealmRole.ROLE_UPLOADER) ?: false &&
                viewingUser?.roles?.contains(DatalandRealmRole.ROLE_REVIEWER) ?: false
            ) ||
            (viewingUser?.roles?.contains(DatalandRealmRole.ROLE_ADMIN) ?: false) ||
            (
                viewingUser?.roles?.contains(DatalandRealmRole.ROLE_USER) ?: false &&
                    companyRoleChecker.doesCurrentUserHaveGivenRoleForCompany(companyId, CompanyRole.CompanyOwner)
                )
    }
}
