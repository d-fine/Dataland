package org.dataland.datalandmessagequeueutils.messages.data

/**
 * The payload for a data upload message for a data point
 */
data class DataPointUploadedPayload(
    val dataPointId: String,
    val companyId: String,
    val companyName: String,
    val dataPointType: String,
    val reportingPeriod: String,
    val uploadTime: Long,
    val uploaderUserId: String,
    val initialQaStatus: String,
    val initialQaComment: String?,
)
