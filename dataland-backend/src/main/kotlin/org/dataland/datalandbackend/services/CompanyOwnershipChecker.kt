package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.model.companies.CompanyInformationPatch
import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.infrastructure.ClientException
import org.dataland.datalandcommunitymanager.openApiClient.model.CompanyRole
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Service to execute company-ownership-checks to decide whether a user can access a resource or not
 * @param dataMetaInformationManager required here to find companyIds for dataIds
 * @param companyRolesControllerApi gets company role assignments from the community manager
 */
@Service("CompanyOwnershipChecker")
class CompanyOwnershipChecker(
    @Autowired private val dataMetaInformationManager: DataMetaInformationManager,
    @Autowired private val companyRolesControllerApi: CompanyRolesControllerApi,
    @Autowired val logMessageBuilder: LogMessageBuilder,
) {
    /**
     * Method to check whether the currently authenticated user is company owner of a specified company and therefore
     * has uploader rights for this company
     * @param companyId the ID of the company
     * @return a Boolean indicating whether the user is company owner or not
     */
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    fun isCurrentUserCompanyOwnerForCompanyOfDataId(dataId: String): Boolean {
        val companyId = dataMetaInformationManager.getDataMetaInformationByDataId(dataId).company.companyId
        return isCurrentUserCompanyOwnerForCompany(companyId)
    }

    /**
     * Method to check whether a given company has at least one company owner
     * @param companyId the ID of the company
     * @return a Boolean indicating whether the company has at least one company owner
     */
    @Transactional(readOnly = true)
    fun companyExistsAndHasNoOwner(companyId: String): Boolean {
        val companyOwners = companyRolesControllerApi.getCompanyRoleAssignments(
            CompanyRole.CompanyOwner, UUID.fromString(companyId),
        )
        return companyOwners.isEmpty()
    }

    // This function can be made more generic if additional field-specific checks are needed in the future
    /**
     * Method to check whether the patch contains only fields that are allowed to be altered by the uploader
     * @param patch the fields to be patched
     * @return a Boolean indicating whether the patch complies with the access requirements
     */
    @Transactional(readOnly = true)
    fun onlyPatchesAuthorizedFields(patch: CompanyInformationPatch): Boolean {
        val unauthorizedFields = mutableListOf<String>()

        if (patch.companyName != null) unauthorizedFields.add("companyName")
        if (patch.companyAlternativeNames != null) unauthorizedFields.add("companyAlternativeNames")
        if (patch.companyLegalForm != null) unauthorizedFields.add("companyLegalForm")
        if (patch.headquarters != null) unauthorizedFields.add("headquarters")
        if (patch.headquartersPostalCode != null) unauthorizedFields.add("headquartersPostalCode")
        if (patch.sector != null) unauthorizedFields.add("sector")
        if (patch.identifiers != null) unauthorizedFields.add("identifiers")
        if (patch.countryCode != null) unauthorizedFields.add("countryCode")
        if (patch.isTeaserCompany != null) unauthorizedFields.add("isTeaserCompany")
        if (patch.parentCompanyLei != null) unauthorizedFields.add("parentCompanyLei")

        if (unauthorizedFields.isNotEmpty()) {
            throw AccessDeniedException(logMessageBuilder.generateInvalidAlterationExceptionMessage(unauthorizedFields))
        }
        return true
    }
}
