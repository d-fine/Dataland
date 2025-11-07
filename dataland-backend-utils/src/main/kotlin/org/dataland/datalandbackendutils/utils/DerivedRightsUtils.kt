package org.dataland.datalandbackendutils.utils

import org.dataland.datalandbackendutils.model.InheritedRole

/**
 * Utility object for user rights derived from inherited roles.
 */
object DerivedRightsUtils {
    /**
     * Checks whether a user with the given map of company IDs to inherited roles of the user in the company is
     * a Dataland member. This is the case if and only if the user has the "DatalandMember" inherited role in at least
     * one company.
     * @param companyIdsToInheritedRoles A map where the keys are company IDs and the values are lists of inherited
     * roles the user has in the respective company.
     * @return true if the user is a Dataland member, false otherwise.
     */
    fun isUserDatalandMember(companyIdsToInheritedRoles: Map<String, List<String>>): Boolean =
        companyIdsToInheritedRoles.values.any { it.contains(InheritedRole.DatalandMember.name) }
}
