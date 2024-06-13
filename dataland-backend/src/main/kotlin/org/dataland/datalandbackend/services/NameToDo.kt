package org.dataland.datalandbackend.services

import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandcommunitymanager.openApiClient.api.DataOwnerControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.infrastructure.ClientException
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Implementation of a (company) data ownership manager for Dataland
 * @param dataOwnerRepository  JPA for data ownership relations
 */
@Service("NameToDo")
class NameToDo( // TODO name
    @Autowired private val dataMetaInformationManager: DataMetaInformationManager,
    @Autowired private val companyQueryManager: CompanyQueryManager,
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
        // TODO sehr komische Funktion... schau später nochmal genauer rein
        val userId = DatalandAuthentication.fromContext().userId
        fun exceptionToThrow(cause: Throwable?) = InsufficientRightsApiException(
            "Neither uploader nor data owner",
            "You don't seem be a data owner of company $companyId, which would be required for uploading this data " +
                "set without general uploader rights.",
            cause,
        )
        try {
            // companyQueryManager.getCompanyById(companyId) // TODO sollte ein eigenes try bekommen
            dataOwnerControllerApi.isUserDataOwnerForCompany(UUID.fromString(companyId), UUID.fromString(userId))
            return true
        } catch (clientException: ClientException) {
            if (clientException.statusCode == 404) {
                return false // TODO vllt kann man irgendwie diese custom expressison oben durchreichen?
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
