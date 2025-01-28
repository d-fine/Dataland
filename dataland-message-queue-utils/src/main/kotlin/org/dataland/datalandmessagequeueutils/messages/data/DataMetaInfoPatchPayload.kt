package org.dataland.datalandmessagequeueutils.messages.data

/**
 * The payload for a data upload message
 */
data class DataMetaInfoPatchPayload(
    override val dataId: String,
    val uploaderUserId: String?,
): DataPayload
