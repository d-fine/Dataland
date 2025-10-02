package org.dataland.datalandbackendutils.interfaces

/**
 * Interface containing the reporting period and company ID that is commonly used by all data dimensions.
 */
interface BaseDimensions {
    val companyId: String
    val reportingPeriod: String
}
