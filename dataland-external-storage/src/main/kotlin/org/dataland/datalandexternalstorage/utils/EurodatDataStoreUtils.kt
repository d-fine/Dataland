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
     * This method will rerun a given method if an exception is thrown while running it
     * @param inputMethod to specify in the logs which method should be rerun
     */
    @Suppress("TooGenericExceptionCaught")
    fun <T> retryWrapperMethod(inputMethod: String, block: () -> T): T {
        var retryCount = 0
        while (retryCount <= maxRetries) {
            try {
                logger.info("Trying to run the method $inputMethod. Try number ${retryCount + 1}.")
                return block()
            } catch (e: Exception) {
                logger.error("An error occurred while executing the method $inputMethod: ${e.message}. Trying again")
                if (retryCount == maxRetries) {
                    logger.error(
                        "An error occurred while executing the method $inputMethod: ${e.message}. " +
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
