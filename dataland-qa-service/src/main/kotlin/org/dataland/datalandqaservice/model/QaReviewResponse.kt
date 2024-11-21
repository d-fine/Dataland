package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReviewEntity

/**
 * Comparable to the QaReviewEntity with the difference that the triggeringUserId is optional.
 * This class is used or the GET Response.
 */
data class QaReviewResponse(
    val dataId: String,
    val companyId: String,
    val companyName: String,
    val dataType: String,
    val reportingPeriod: String,
    val timestamp: Long,
    var qaStatus: QaStatus,
    val comment: String?,
    val triggeringUserId: String?,
)

/**
 * Converts the QaReviewEntity into a QaReviewResponse which is used in a response for a GET Request.
 * The QaReviewResponse can optionally hide the triggeringUserId by setting showTriggeringUserId to false.
 */
fun QaReviewEntity.toQaReviewResponse(showTriggeringUserId: Boolean = false) =
    QaReviewResponse(
        dataId = this.dataId,
        companyId = this.companyId,
        companyName = this.companyName,
        dataType = this.dataType,
        reportingPeriod = this.reportingPeriod,
        timestamp = this.timestamp,
        qaStatus = this.qaStatus,
        comment = this.comment,
        triggeringUserId = if (showTriggeringUserId) this.triggeringUserId else null,
    )
