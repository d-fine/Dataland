package org.dataland.datalandmessagequeueutils.messages.data

/**
 * The payload for a data upload message
 */
data class DataMetaInfoPatchMessage(
    val dataId: String,
    val uploaderUserId: String?,
)
