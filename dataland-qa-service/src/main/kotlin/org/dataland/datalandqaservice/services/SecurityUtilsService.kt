package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Implements a utility function that can be used e.g., in PRE_AUTHORIZE
 * for several one authentication use-case.
 */
@Service("SecurityUtilsService")
class SecurityUtilsService(
    @Autowired val metaDataControllerApi: MetaDataControllerApi,
) {

    /**
     * This function checks whether the user uploaded the dataset with the corresponding identifier.
     */
    fun isUserAskingQaReviewStatusOfUploadedDataset(identifier: String): Boolean {
        val dataMetaInformation = metaDataControllerApi.getDataMetaInfo(identifier)
        // TODO Catch exceptions
        // SecurityContextHolder.getContext().authentication.name
        return DatalandAuthentication.fromContext().userId == dataMetaInformation.uploaderUserId
    }
}
