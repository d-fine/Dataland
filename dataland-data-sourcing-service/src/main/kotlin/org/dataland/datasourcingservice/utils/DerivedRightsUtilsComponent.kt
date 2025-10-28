package org.dataland.datasourcingservice.utils

import org.dataland.datalandbackendutils.utils.DerivedRightsUtils
import org.dataland.datalandcommunitymanager.openApiClient.api.InheritedRolesControllerApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Utility bean for functionality concerning derived rights.
 */
@Component("DerivedRightsUtilsComponent")
class DerivedRightsUtilsComponent(
    @Autowired private val inheritedRolesControllerApi: InheritedRolesControllerApi,
) {
    /**
     * Check whether the specified user is a premium user based on their inherited roles.
     * @param userId the Dataland ID of the user in question
     * @return true if the user is a premium user, false otherwise
     */
    fun isUserPremiumUser(userId: String): Boolean =
        DerivedRightsUtils.isUserPremiumUser(
            inheritedRolesControllerApi.getInheritedRoles(userId),
        )
}
