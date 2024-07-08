package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

/**
 * Implements a utility function that can be used e.g., in PRE_AUTHORIZE
 * for several one authentication use-case.
 */
@Service("SecurityUtilsService")
class SecurityUtilsService(
    @Autowired val metaDataControllerApi: MetaDataControllerApi,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * This function checks whether the user uploaded the dataset with the corresponding identifier.
     */
    fun isUserAskingQaReviewStatusOfUploadedDataset(dataId: UUID): Boolean {
        logger.info("########################################################")
        logger.info("Checking if user created the dataset $dataId")
        val dataMetaInformation = metaDataControllerApi.getDataMetaInfo(dataId.toString())
        // TODO Catch exceptions
        return DatalandAuthentication.fromContext().userId == dataMetaInformation.uploaderUserId
    }
}
