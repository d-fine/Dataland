package org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils

/**
 * A class to filter for specific QA Review entries
 */
data class DataPointQaReviewItemFilter(
    val companyId: String?,
    val dataPointIdentifier: String?,
    val reportingPeriod: String?,
    val qaStatus: String?,
)
