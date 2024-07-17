package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.model.companies.CompanyInformationPatch
import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.infrastructure.ClientException
import org.dataland.datalandcommunitymanager.openApiClient.model.CompanyRole
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.util.*
import kotlin.reflect.full.memberProperties

/**
 * Service to execute company-ownership-checks to decide whether a user can access a resource or not
 * @param dataMetaInformationManager required here to find companyIds for dataIds
 * @param companyRolesControllerApi gets company role assignments from the community manager
 */
@Service("CompanyRoleChecker")
class CompanyRoleChecker(
    @Autowired private val dataMetaInformationManager: DataMetaInformationManager,
    @Autowired private val companyRolesControllerApi: CompanyRolesControllerApi,
    @Autowired private val companyQueryManager: CompanyQueryManager,
) {
    /**
     * Checks whether the currently authenticated user has a company role for a specified company.
     * @param companyId the ID of the company
     * @param role for which the check shall happen
     * @return a Boolean indicating whether the user has the role or not
     */
    fun hasCurrentUserGivenRoleForCompany(companyId: String, role: CompanyRole): Boolean {
        val userId = DatalandAuthentication.fromContext().userId
        return try {
            companyRolesControllerApi.hasUserCompanyRole(
                role, UUID.fromString(companyId),
                UUID.fromString(userId),
            )
            true
        } catch (clientException: ClientException) {
            if (clientException.statusCode == HttpStatus.NOT_FOUND.value()) {
                false
            } else {
                throw clientException
            }
        }
    }

    /**
     * Checks whether the current user has any company role for the companyId
     * @param companyId to check the roles for
     * @returns a boolean stating if the user has any company role for the company
     */
    fun hasCurrentUserAnyRoleForCompany(companyId: String): Boolean {
        val userId = UUID.fromString(DatalandAuthentication.fromContext().userId)
        val roles = companyRolesControllerApi.getCompanyRoleAssignments(
            companyId = UUID.fromString(companyId),
            userId = userId,
        )
        return roles.isNotEmpty()
    }

    /**
     * Checks whether the currently authenticated user has the role for the specified company that is
     * associated with a specific framework dataset.
     * @param dataId of the framework dataset
     * @param role to check for
     * @return a Boolean indicating whether the user has the role for the company associated with the dataset
     */
    fun hasCurrentUserGivenRoleForCompanyOfDataId(dataId: String, role: CompanyRole): Boolean {
        val companyId = dataMetaInformationManager.getDataMetaInformationByDataId(dataId).company.companyId
        return hasCurrentUserGivenRoleForCompany(companyId, role)
    }

    /**
     * Checks whether the current user has any company role for the companyId associated with the dataId
     * @param dataId of the dataset to get the associated company for
     * @returns a boolean stating if the user has any company role for the company associated with the dataId
     */
    fun hasCurrentUserAnyRoleForCompanyOfDataId(dataId: String): Boolean {
        val companyId = dataMetaInformationManager.getDataMetaInformationByDataId(dataId).company.companyId
        return hasCurrentUserAnyRoleForCompany(companyId)
    }

    /**
     * Checks whether a given company has at least one company owner
     * @param companyId the ID of the company
     * @return a Boolean indicating whether the company has at least one company owner
     */
    fun isCompanyExistentAndWithoutOwner(companyId: String): Boolean {
        companyQueryManager.verifyCompanyIdExists(companyId)
        return try {
            companyRolesControllerApi.hasCompanyAtLeastOneOwner(UUID.fromString(companyId))
            false
        } catch (clientException: ClientException) {
            if (clientException.statusCode == HttpStatus.NOT_FOUND.value()) {
                true
            } else {
                throw clientException
            }
        }
    }

    /**
     * Method to check whether the patch contains only fields
     * that are allowed to be altered by a non-keycloak-admin-user
     * @param patch the fields to be patched
     * @return a Boolean indicating whether the patch complies with the access requirements
     */
    fun areOnlyAuthorizedFieldsPatched(patch: CompanyInformationPatch): Boolean {
        val unauthorizedFields = CompanyInformationPatch::class.memberProperties
            .filter { property ->
                property.get(patch) != null &&
                    property.name != "companyContactDetails" &&
                    property.name != "website"
            }
            .map { it.name }

        if (unauthorizedFields.isNotEmpty()) {
            throw InsufficientRightsApiException(
                "Invalid alteration attempt",
                "You do not have the required permission to change the following fields:" +
                    " ${unauthorizedFields.joinToString(", ")}.\n" +
                    " You are only allowed to change the fields \"website\" and \"companyContactDetails\".",
            )
        }
        return true
    }

    /**
     * A user can bypass QA if
     * (a) the user has reviewer-rights
     * (b) the user has the company uploader or company owner role for the company associated with the data upload
     * This function checks these conditions.
     * @param authenticationContext is the current auth context of this thread
     * @param companyId of the company associated with the data upload for which Qa shall be bypassed
     * @returns a boolean that states if the user is allowed to bypass Qa or not
     */
    fun canUserBypassQa(authenticationContext: DatalandAuthentication?, companyId: String): Boolean {
        val userUUID = UUID.fromString(DatalandAuthentication.fromContext().userId)
        val companyUUID = UUID.fromString(companyId)

        return authenticationContext?.roles?.contains(DatalandRealmRole.ROLE_REVIEWER) == true ||
            companyRolesControllerApi.getCompanyRoleAssignments(null, companyUUID, userUUID)
                .any { it.companyRole == CompanyRole.CompanyOwner || it.companyRole == CompanyRole.DataUploader }
    }
}
