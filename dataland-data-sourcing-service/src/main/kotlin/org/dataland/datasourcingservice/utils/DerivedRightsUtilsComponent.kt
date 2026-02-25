package org.dataland.datasourcingservice.utils

import org.dataland.datalandbackendutils.utils.DerivedRightsUtils
import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.api.InheritedRolesControllerApi
import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Utility bean for functionality concerning derived rights.
 */
@Component("DerivedRightsUtilsComponent")
class DerivedRightsUtilsComponent(
    @Autowired private val inheritedRolesControllerApi: InheritedRolesControllerApi,
    @Autowired private val companyRolesControllerApi: CompanyRolesControllerApi,
) {
    /**
     * Check whether the specified user is a Dataland member based on their inherited roles.
     * @param userId the Dataland ID of the user in question
     * @return true if the user is a Dataland member, false otherwise
     */
    fun isUserDatalandMemberOrAdmin(userId: String): Boolean =
        DerivedRightsUtils.isUserDatalandMember(
            inheritedRolesControllerApi.getInheritedRoles(userId),
        ) ||
            isUserAdmin()

    /**
     * Check whether the current user is an admin.
     * @return true if the current user is an admin, false otherwise
     */
    fun isCurrentUserAdmin(): Boolean = isUserAdmin()

    /**
     * Check whether the current user is a provider (document collector or data extractor)
     * for the given data sourcing entity.
     * @param entity the data sourcing entity to check against
     * @return true if the current user is a provider for the entity, false otherwise
     */
    fun isCurrentUserProviderFor(entity: DataSourcingEntity): Boolean {
        val userId = DatalandAuthentication.fromContextOrNull()?.userId ?: return false
        return listOfNotNull(entity.documentCollector, entity.dataExtractor).any { companyId ->
            companyRolesControllerApi
                .getExtendedCompanyRoleAssignments(
                    userId = ValidationUtils.convertToUUID(userId),
                    companyId = companyId,
                ).isNotEmpty()
        }
    }
}
