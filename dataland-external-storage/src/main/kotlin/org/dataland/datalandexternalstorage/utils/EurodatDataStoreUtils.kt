package org.dataland.datalandexternalstorage.utils

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Enables a centralized generation of log messages for all Dataland backend operations.
 */

@Component("EurodatDataStoreUtils")
object EurodatDataStoreUtils {
    private const val maxRetries = 8
    private const val millisecondsBetweenRetries = 15000
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * This method will rerun a given method for a specified number of times if an exception is thrown while running it
     * @param descriptionOfOperation to specify in the logs which method should be rerun
     * @param block the method that should be retried
     * @returns the value T of the input method
     */
    @Suppress("TooGenericExceptionCaught")
    fun <T> retryWrapperMethod(descriptionOfOperation: String, block: () -> T): T {
        var retryCount = 0
        while (retryCount <= maxRetries) {
            try {
                logger.info("Trying to run: $descriptionOfOperation. Try number ${retryCount + 1}.")
                return block()
            } catch (e: Exception) {
                logger.error(
                    "An error occurred while trying to $descriptionOfOperation: ${e.message}. " +
                        "Trying again",
                )
                if (retryCount == maxRetries) {
                    logger.error(
                        "An error occurred while trying to $descriptionOfOperation: ${e.message}. " +
                            "Process terminated",
                    )
                    throw e
                }
            }
            retryCount++
            Thread.sleep(millisecondsBetweenRetries.toLong())
        }
        return block()
    }
}
