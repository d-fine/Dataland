package org.dataland.datalandqaservice.model

import org.dataland.datalandbackendutils.model.QaStatus

/**
 * API response model for a non-sourceability QA review entry.
 */
data class NonSourceableQaReviewInformation(
    val nonSourceabilityId: String,
    val companyId: String,
    val dataType: String,
    val reportingPeriod: String,
    val qaStatus: QaStatus,
    val reason: String?,
    val uploaderUserId: String,
    val uploadTime: Long,
    val reviewerUserId: String?,
    val qaComment: String?,
)
