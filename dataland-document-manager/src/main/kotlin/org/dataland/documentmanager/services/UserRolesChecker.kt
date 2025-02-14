package org.dataland.documentmanager.services

import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.model.CompanyRole
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Service to execute company-role-checks to decide whether a user can access a resource or not
 * @param companyRolesControllerApi gets company role assignments from the community manager
 */
@Service("UserRolesChecker")
class UserRolesChecker(
    @Autowired private val companyRolesControllerApi: CompanyRolesControllerApi,
    @Autowired private val documentManager: DocumentManager,
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
     * Method to check whether the currently authenticated user is uploader of the document. If so, patching the meta
     * info of the document shall be permitted.
     * @param documentId identifier of document
     * @return a Boolean indicating whether the user is uploader of the document
     */
    @Transactional(readOnly = true)
    fun isCurrentUserUploaderOfDocument(documentId: String): Boolean {
        val userId = DatalandAuthentication.fromContext().userId
        return documentManager
            .retrieveDocumentMetaInfo(
                documentId,
            ).uploaderId == userId
    }
}
