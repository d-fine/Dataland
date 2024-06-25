package org.dataland.documentmanager.services

import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.infrastructure.ClientException
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Service to execute company-ownership-checks to decide whether a user can access a resource or not
 * @param companyRolesControllerApi gets company role assignments from the community manager
 */
@Service("UserRolesChecker")
class UserRolesChecker(
    @Autowired private val companyRolesControllerApi: CompanyRolesControllerApi,
) {

    /**
     * Method to check whether the currently authenticated user is company owner of any company and therefore
     * has document uploader rights
     * @return a Boolean indicating whether the user is a company owner of any company or not
     */
    @Transactional(readOnly = true)
    fun isCurrentUserCompanyOwner(): Boolean {
        val userId = DatalandAuthentication.fromContext().userId
        return try {
            companyRolesControllerApi.hasUserCompanyOwnerRole(UUID.fromString(userId))
            true
        } catch (clientException: ClientException) {
            if (clientException.statusCode == HttpStatus.NOT_FOUND.value()) {
                false
            } else {
                throw clientException
            }
        }
    }
}
