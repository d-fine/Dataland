package org.dataland.datalandmessagequeueutils.messages.data

/**
 * The payload for a data upload message
 */
data class DataMetaInfoPatchPayload(
    val dataId: String,
    val uploaderId: String?,
)
