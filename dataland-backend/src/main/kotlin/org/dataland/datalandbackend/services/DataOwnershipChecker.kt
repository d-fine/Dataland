package org.dataland.datalandbackend.services

import org.dataland.datalandcommunitymanager.openApiClient.api.DataOwnerControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.infrastructure.ClientException
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Service to execute data-ownership-checks to decide whether a user can access a resource or not
 * @param dataMetaInformationManager required here to find companyIds for dataIds
 * @param dataOwnerControllerApi gets data-ownership data from the community manager
 */
@Service("DataOwnershipChecker")
class DataOwnershipChecker(
    @Autowired private val dataMetaInformationManager: DataMetaInformationManager,
    @Autowired private val dataOwnerControllerApi: DataOwnerControllerApi,
) {

    /**
     * Method to check whether the currently authenticated user is data owner of a specified company and therefore
     * has uploader rights for this company
     * @param companyId the ID of the company
     * @return a Boolean indicating whether the user is data owner or not
     */
    @Transactional(readOnly = true)
    fun isCurrentUserDataOwnerForCompany(companyId: String): Boolean {
        val userId = DatalandAuthentication.fromContext().userId
        return try {
            dataOwnerControllerApi.isUserDataOwnerForCompany(UUID.fromString(companyId), UUID.fromString(userId))
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
     * Method to check whether the currently authenticated user is data owner of the specified company that is
     * associated with a specific framework dataset.
     * @param dataId of the framework dataset
     * @return a Boolean indicating whether the user is data owner of the company associated with the dataset
     */
    @Transactional(readOnly = true)
    fun isCurrentUserDataOwnerForCompanyOfDataId(dataId: String): Boolean {
        val companyId = dataMetaInformationManager.getDataMetaInformationByDataId(dataId).company.companyId
        return isCurrentUserDataOwnerForCompany(companyId)
    }
}
