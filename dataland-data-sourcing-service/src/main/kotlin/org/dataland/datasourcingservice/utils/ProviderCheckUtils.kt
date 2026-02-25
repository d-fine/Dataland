package org.dataland.datasourcingservice.utils

import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import java.util.UUID

/**
 * Returns the set of company IDs for which the current user has a provider role.
 * Makes a single API call without filtering by company, suitable for batch operations.
 */
fun CompanyRolesControllerApi.getCurrentUserProviderCompanyIds(): Set<UUID> {
    val userId = DatalandAuthentication.fromContextOrNull()?.userId ?: return emptySet()
    return getExtendedCompanyRoleAssignments(userId = ValidationUtils.convertToUUID(userId))
        .map { UUID.fromString(it.companyId) }
        .toSet()
}

/**
 * Returns whether the current user is a provider (document collector or data extractor)
 * for the given data sourcing entity.
 */
fun CompanyRolesControllerApi.isCurrentUserProviderFor(entity: DataSourcingEntity): Boolean {
    val userId = DatalandAuthentication.fromContextOrNull()?.userId ?: return false
    return listOfNotNull(entity.documentCollector, entity.dataExtractor).any { companyId ->
        getExtendedCompanyRoleAssignments(
            userId = ValidationUtils.convertToUUID(userId),
            companyId = companyId,
        ).isNotEmpty()
    }
}
