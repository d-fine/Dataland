package org.dataland.documentmanager.services

import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.model.CompanyRole
import org.dataland.documentmanager.model.DocumentMetaInfoPatch
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.reflect.full.memberProperties

/**
 * Service to execute company-role-checks to decide whether a user can access a resource or not
 * @param companyRolesControllerApi gets company role assignments from the community manager
 */
@Service("UserRolesChecker")
class UserRolesChecker(
    @Autowired private val companyRolesControllerApi: CompanyRolesControllerApi,
) {
    /**
     * Method to check whether the currently authenticated user is company owner or company uploader of any company and
     * therefore has document uploader rights
     * @return a Boolean indicating whether the user is a company owner or uploader of any company or not
     */
    @Transactional(readOnly = true)
    fun isCurrentUserCompanyOwnerOrCompanyUploader(): Boolean {
        val userId = DatalandAuthentication.fromContext().userId
        val roles =
            companyRolesControllerApi
                .getCompanyRoleAssignments(
                    null,
                    null,
                    UUID.fromString(userId),
                ).map { it.companyRole }
        return roles.contains(CompanyRole.CompanyOwner) || roles.contains(CompanyRole.DataUploader)
    }

    /**
     * Checks whether the patch contains only fields that a non-admin keycloak user is allowed to patch.
     * Currently, uploaders are only permitted to patch fields 'companyIds' and 'reportingPeriods'.
     * @return a Boolean indicating whether the user abides by these rules.
     */
    @Transactional(readOnly = true)
    fun areOnlyAllowedFieldsPatched(documentMetaInfoPatch: DocumentMetaInfoPatch): Boolean {
        if (DatalandAuthentication.fromContext().roles.contains(DatalandRealmRole.ROLE_ADMIN)) {
            return true
        }
        val unauthorizedFields =
            DocumentMetaInfoPatch::class
                .memberProperties
                .filter { property ->
                    property.get(documentMetaInfoPatch) != null &&
                        property.name != "companyIds" &&
                        property.name != "reportingPeriods"
                }.map { it.name }

        if (unauthorizedFields.isNotEmpty()) {
            throw InsufficientRightsApiException(
                summary = "Invalid patch attempt",
                message =
                    "You do not have the required permission to change the following fields:" +
                        " ${unauthorizedFields.joinToString(", ")}." +
                        " You are only allowed to update the fields companyIds and reportingPeriods.",
            )
        }
        return true
    }
}
