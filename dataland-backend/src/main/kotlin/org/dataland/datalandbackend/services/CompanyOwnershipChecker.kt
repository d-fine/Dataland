package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.model.companies.CompanyInformationPatch
import org.dataland.datalandbackendutils.exceptions.InvalidPatchApiException
import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.infrastructure.ClientException
import org.dataland.datalandcommunitymanager.openApiClient.model.CompanyRole
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
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
@Service("CompanyOwnershipChecker")
class CompanyOwnershipChecker(
    @Autowired private val dataMetaInformationManager: DataMetaInformationManager,
    @Autowired private val companyRolesControllerApi: CompanyRolesControllerApi,
    @Autowired private val companyQueryManager: CompanyQueryManager,
) {
    /**
     * Method to check whether the currently authenticated user is company owner of a specified company and therefore
     * has uploader rights for this company
     * @param companyId the ID of the company
     * @return a Boolean indicating whether the user is company owner or not
     */
    fun isCurrentUserCompanyOwnerForCompany(companyId: String): Boolean {
        val userId = DatalandAuthentication.fromContext().userId
        return try {
            companyRolesControllerApi.hasUserCompanyRole(
                CompanyRole.CompanyOwner, UUID.fromString(companyId),
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
     * Method to check whether the currently authenticated user is company owner of the specified company that is
     * associated with a specific framework dataset.
     * @param dataId of the framework dataset
     * @return a Boolean indicating whether the user is company owner of the company associated with the dataset
     */
    fun isCurrentUserCompanyOwnerForCompanyOfDataId(dataId: String): Boolean {
        val companyId = dataMetaInformationManager.getDataMetaInformationByDataId(dataId).company.companyId
        return isCurrentUserCompanyOwnerForCompany(companyId)
    }

    /**
     * Method to check whether a given company has at least one company owner
     * @param companyId the ID of the company
     * @return a Boolean indicating whether the company has at least one company owner
     */
    fun doesCompanyExistsAndHaveNoOwner(companyId: String): Boolean {
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
            throw InvalidPatchApiException(
                "Invalid alteration attempt",
                " You do not have the required permission to change the following fields:" +
                        " ${unauthorizedFields.joinToString(", ")}",
            )
        }
        return true
    }
}
