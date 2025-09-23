package org.dataland.datasourcingservice.utils

import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Utility service class for security-related operations.
 */
@Service
class SecurityUtilsService(
    @Autowired private val companyRolesControllerApi: CompanyRolesControllerApi,
) {
    /**
     * Returns true if the user is member of the company
     * @param companyId dataland companyId
     */
    @Transactional(readOnly = true)
    fun isUserMemberOfTheCompany(companyId: UUID?): Boolean {
        val userId = SecurityContextHolder.getContext().authentication.name
        if (companyId == null || userId == null) return false
        return companyRolesControllerApi
            .getExtendedCompanyRoleAssignments(
                role = null,
                companyId = companyId,
                userId = UUID.fromString(userId),
            ).isNotEmpty()
    }
}
