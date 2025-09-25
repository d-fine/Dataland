package org.dataland.datalandbackendutils.utils

import org.dataland.datalandbackendutils.interfaces.BaseDimensions
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
        } catch (ignore: Exception) {
            return false
        }
    }

    fun isBaseDimensions(baseDimensions: BaseDimensions) =
        isCompanyId(baseDimensions.companyId) && isReportingPeriod(baseDimensions.reportingPeriod)
}
