package org.dataland.datalandmessagequeueutils.messages.data

/**
 * The payload for a data upload message for a datapoint
 */
data class DataPointUploadedPayload(
    val dataId: String,
    val initialQaStatus: String,
    val initialQaComment: String?,
)
