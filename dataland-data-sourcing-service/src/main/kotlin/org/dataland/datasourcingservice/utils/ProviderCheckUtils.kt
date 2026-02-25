package org.dataland.datasourcingservice.utils

import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.keycloakAdapter.auth.DatalandAuthentication

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
