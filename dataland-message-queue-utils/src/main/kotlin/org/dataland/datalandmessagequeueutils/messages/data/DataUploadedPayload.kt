package org.dataland.datalandmessagequeueutils.messages.data

/**
 * The payload for a data upload message
 */
data class DataUploadedPayload(
    val dataId: String,
    val bypassQa: Boolean,
)
