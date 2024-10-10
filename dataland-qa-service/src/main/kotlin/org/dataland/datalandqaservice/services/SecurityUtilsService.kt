package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

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
    fun userAskingQaReviewStatusOfOwnDataset(dataId: UUID): Boolean {
        logger.info("Checking if user created the dataset $dataId")
        try {
            val dataMetaInformation = metaDataControllerApi.getDataMetaInfo(dataId.toString())
            return DatalandAuthentication.fromContext().userId == dataMetaInformation.uploaderUserId
        } catch (_: ClientException) {
            logger.info("Unable to find the dataset $dataId")
            return false
        }
    }
}
