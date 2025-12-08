package org.dataland.datalandbackendutils.utils

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.interfaces.BaseDimensions
import java.util.UUID

object ValidationUtils {
    private val uuidRegex =
        Regex(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\$",
        )

    val reportingPeriodMininum = 2010
    val reportingPeriodMaximum = 2039
    private val reportingPeriodRegex = Regex((reportingPeriodMininum..reportingPeriodMaximum).joinToString("|"))

    /**
     * Checks if the given string corresponds to a reporting period.
     * @param testString the string to check
     * @return true if the string matches the reporting period format (e.g., "2020", "2021", etc.) false otherwise
     */
    fun isReportingPeriod(testString: String) = testString.matches(Regex(reportingPeriodRegex))

    /**
     * Checks if the given string is a valid UUID.
     * @param testString the string to check
     * @return true if the string is a valid UUID, false otherwise
     */
    fun isUuid(testString: String): Boolean = uuidRegex.matches(testString)

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
        isUuid(baseDimensions.companyId) && isReportingPeriod(baseDimensions.reportingPeriod)
}
