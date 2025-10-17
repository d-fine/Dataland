package org.dataland.datalandbackendutils.utils

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import java.util.UUID

object ValidationUtils {
    /**
     * Checks if the given string corresponds to a reporting period.
     * @param testString the string to check
     * @return true if the string matches the reporting period format (e.g., "2020", "2021", etc.) false otherwise
     */
    fun isReportingPeriod(testString: String) = testString.matches(Regex("20[1-3][0-9]"))

    /**
     * Checks if the given string conforms to the expected company ID format.
     * @param testString the string to check
     * @return true if the string conforms to the company ID format, false otherwise
     */
    fun isCompanyId(testString: String) = isUuid(testString)

    /**
     * Checks if the given string is a valid UUID by trying to construct a UUID from it.
     * @param testString the string to check
     * @return true if the string is a valid UUID, false otherwise
     */
    private fun isUuid(testString: String): Boolean {
        try {
            UUID.fromString(testString)
            return true
        } catch (_: Exception) {
            return false
        }
    }

    /**
     * Converts the given string to a UUID, throwing an IllegalArgumentException if the string is not a valid UUID.
     * @param testString the string to convert
     * @return the UUID corresponding to the string
     * @throws ResourceNotFoundApiException if the string is not a valid UUID (so there is no resource with such an ID)
     */
    fun convertToUUID(testString: String): UUID =
        try {
            UUID.fromString(testString)
        } catch (_: IllegalArgumentException) {
            throw ResourceNotFoundApiException(
                summary = "Unknown ID.",
                message = "The string $testString is not a valid UUID. In particular, the resource requested under it is unknown.",
            )
        }
}
