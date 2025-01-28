package org.dataland.datalandmessagequeueutils.messages.data

/**
 * The payload for a data upload message
 */
data class DataIdPayload(
    override val dataId: String,
) : DataPayload
