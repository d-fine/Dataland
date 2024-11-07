package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReviewLogEntity

/**
 * Comparable to the DatasetQaReviewLogEntity with the difference that the reviewerId is optional.
 * This class is used or the GET Response.
 */
data class QaReviewResponse(
    val dataId: String,
    val companyId: String,
    val companyName: String,
    val dataType: DataTypeEnum,
    val reportingPeriod: String,
    val timestamp: Long,
    var qaStatus: QaStatus,
    val comment: String?,
    val reviewerId: String?,
)

/**
 * Converts the DatasetQaReviewLogEntity into a DatasetQaReviewResponse which is used in a response for a GET Request.
 * The DatasetQaReviewResponse can optionally hide the reviewerId by setting showReviewerId to false.
 */
fun QaReviewLogEntity.toDatasetQaReviewResponse(showReviewerId: Boolean = false) =
    QaReviewResponse(
        dataId = this.dataId,
        companyId = this.companyId,
        companyName = this.companyName,
        dataType = this.dataType,
        reportingPeriod = this.reportingPeriod,
        timestamp = this.timestamp,
        qaStatus = this.qaStatus,
        comment = this.comment,
        reviewerId = if (showReviewerId) this.reviewerId else null,
    )
