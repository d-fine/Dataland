package org.dataland.datalandexternalstorage.services

import jakarta.annotation.PostConstruct
import java.net.HttpURLConnection
import org.dataland.datalandeurodatclient.openApiClient.api.SafeDepositDatabaseResourceApi
import org.dataland.datalandeurodatclient.openApiClient.infrastructure.ClientException
import org.dataland.datalandeurodatclient.openApiClient.model.SafeDepositDatabaseRequest
import org.dataland.datalandeurodatclient.openApiClient.model.SafeDepositDatabaseResponse
import org.dataland.datalandexternalstorage.utils.EurodatDataStoreUtils.retryWrapperMethod
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * Simple implementation of the initialization of the eurodat minabo safedepositbox
 * @param safeDepositDatabaseResourceClient the EuroDaT client to create the safe deposit box used to store private data
 * on eurodat
 */
@ConditionalOnProperty(
    name = ["dataland.eurodatclient.initialize-safe-deposit-box"], havingValue = "true",
    matchIfMissing = false,
)
@Component
class EurodatSafeDepositBoxInitializer(
    @Autowired var safeDepositDatabaseResourceClient: SafeDepositDatabaseResourceApi,
    @Value("\${dataland.eurodatclient.app-name}")
    private val eurodatAppName: String,
    @Value("\${dataland.eurodatclient.ignore-error}")
    private val isErrorIgnored: Boolean,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Tries to create a safe deposit box in EuroDaT for storage of Dataland data a pre-defined number of times and
     * then throws a final exception after the retries are used up.
     */
    @Suppress("TooGenericExceptionCaught")
    @PostConstruct
    fun createSafeDepositBox() {
        logger.info("Checking if safe deposit box exits. If not creating safe deposit box")
        if (isErrorIgnored) {
            try { retryWrapperMethod("create SafeDepositBox in EuroDaT") {
                isSafeDepositBoxAvailable()
            } } catch (e: Exception) {
                logger.error(
                    "An error occurred while trying to create the EuroDaT SafeDepositBox: ${e.message}.",
                )
            }
        } else {
            retryWrapperMethod("create SafeDepositBox in EuroDaT") {
                isSafeDepositBoxAvailable()
            }
        }
    }

    private fun isSafeDepositBoxAvailable() {
        try {
            postSafeDepositBoxCreationRequest()
        } catch (exception: ClientException) {
            if (exception.statusCode == HttpURLConnection.HTTP_CONFLICT) {
                logger.info("SafeDepositBox already exists.")
            } else {
                throw exception
            }
        }
    }

    /**
     * Sends a POST request to the safe deposit box creation endpoint of the EuroDaT client.
     */
    fun postSafeDepositBoxCreationRequest(): SafeDepositDatabaseResponse {
        val creationRequest = SafeDepositDatabaseRequest(eurodatAppName)
        return safeDepositDatabaseResourceClient.apiV1ClientControllerDatabaseServicePost(creationRequest)
    }
}
