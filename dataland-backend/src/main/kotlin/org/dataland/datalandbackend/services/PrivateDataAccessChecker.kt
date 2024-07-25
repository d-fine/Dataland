package org.dataland.datalandbackend.services

import org.dataland.datalandcommunitymanager.openApiClient.api.RequestControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.infrastructure.ClientException
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.util.*

/**
 * Service to execute access to private data checks to decide whether a user can access a resource or not
 * @param dataMetaInformationManager required here to find companyIds for dataIds
 * @param requestManager gets the access status for a dataset from the community manager
 */
@Service("PrivateDataAccessChecker")
class PrivateDataAccessChecker(
    @Autowired private val dataMetaInformationManager: DataMetaInformationManager,
    @Autowired private val requestManager: RequestControllerApi,
) {
    /**
     * Checks whether the user who is currently authenticated has access to a certain dataset.
     * @param dataId the ID of the dataset
     * @return a Boolean indicating whether the user has access or not
     */
    fun hasUserAccessToPrivateResources(dataId: String): Boolean {
        val metaDataEntity = dataMetaInformationManager.getDataMetaInformationByDataId(dataId)
        val userId = DatalandAuthentication.fromContext().userId
        return try {
            requestManager.hasAccessToDataset(
                UUID.fromString(metaDataEntity.company.companyId),
                metaDataEntity.dataType, metaDataEntity.reportingPeriod,
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
}
