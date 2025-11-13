package org.dataland.datalandbackendutils.utils

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.interfaces.BaseDimensions
import java.util.UUID

object ValidationUtils {
    private val uuidRegex =
        Regex(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\$",
        )

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
        if (!uuidRegex.matches(testString)) return false
        return try {
            UUID.fromString(testString)
            true
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    /**
     * Converts the given string to a UUID, throwing an ResourceNotFoundApiException if the string is not a valid UUID.
     * @param testString the string to convert
     * @return the UUID corresponding to the string
     * @throws ResourceNotFoundApiException if the string is not a valid UUID (so there is no resource with such an ID)
     */
    fun convertToUUID(testString: String): UUID {
        if (!isUuid(testString)) {
            throw ResourceNotFoundApiException(
                summary = "Unknown ID.",
                message = "The string '$testString' is not a valid UUID; the requested resource is unknown.",
            )
        }
        return UUID.fromString(testString)
    }

    /**
     * Checks if a given base dimension contains valid reporting period and company ID
     * @param baseDimensions the base dimension to be checked
     * @return a boolean indicating if it is a valid base dimension or not
     */
    fun isBaseDimensions(baseDimensions: BaseDimensions) =
        isCompanyId(baseDimensions.companyId) && isReportingPeriod(baseDimensions.reportingPeriod)
}
