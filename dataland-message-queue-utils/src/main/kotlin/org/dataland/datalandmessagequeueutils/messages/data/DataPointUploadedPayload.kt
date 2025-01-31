package org.dataland.datalandmessagequeueutils.messages.data

/**
 * The payload for a data upload message for a data point
 */
data class DataPointUploadedPayload(
    val dataPointId: String,
    val initialQaStatus: String,
    val initialQaComment: String?,
)
