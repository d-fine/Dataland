package org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils

import org.dataland.datalandbackendutils.model.QaStatus

/**
 * A class to filter for specific QA Review entries
 */
data class DataPointQaReviewItemFilter(
    val companyId: String?,
    val dataPointType: String?,
    val reportingPeriod: String?,
    val qaStatus: QaStatus?,
)
