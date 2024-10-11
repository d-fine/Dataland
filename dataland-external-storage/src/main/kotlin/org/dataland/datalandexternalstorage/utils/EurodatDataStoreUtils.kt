package org.dataland.datalandexternalstorage.utils

import org.slf4j.LoggerFactory

/**
 * The object holds utils used in the EurodatDataStore class
 */

object EurodatDataStoreUtils {
    private const val MAX_RETRIES = 8
    private const val MILLISECONDS_BETWEEN_RETRIES = 15000
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * This method will rerun a given method for a specified number of times if an exception is thrown while running it
     * @param descriptionOfOperation to specify in the logs what was retried
     * @param block the method that should be retried
     * @returns the value T of the input method
     */
    @Suppress("TooGenericExceptionCaught")
    fun <T> retryWrapperMethod(
        descriptionOfOperation: String,
        block: () -> T,
    ): T {
        var retryCount = 0
        while (retryCount <= MAX_RETRIES) {
            try {
                logger.info("Trying to run: $descriptionOfOperation. Try number ${retryCount + 1}.")
                return block()
            } catch (e: Exception) {
                logger.error(
                    "An error occurred while trying to $descriptionOfOperation: ${e.message}. " +
                        "Trying again",
                )
                if (retryCount == MAX_RETRIES) {
                    logger.error(
                        "An error occurred while trying to $descriptionOfOperation: ${e.message}. " +
                            "Process terminated",
                    )
                    throw e
                }
            }
            retryCount++
            Thread.sleep(MILLISECONDS_BETWEEN_RETRIES.toLong())
        }
        return block()
    }
}
