package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandbackendutils.model.InheritedRole
import org.dataland.datalandcommunitymanager.model.companyRights.CompanyRight

/**
 * Utility object for user roles inherited from company rights.
 */
object InheritedRolesUtils {
    /**
     * Get the (company-specific) inherited roles of the user based on the rights of the company.
     */
    fun getInheritedRoles(companyRights: List<CompanyRight>): List<InheritedRole> =
        if (companyRights.contains(CompanyRight.Member)) {
            listOf(InheritedRole.DatalandMember)
        } else {
            emptyList()
        }
}
