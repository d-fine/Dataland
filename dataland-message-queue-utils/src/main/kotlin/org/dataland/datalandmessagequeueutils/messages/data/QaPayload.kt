package org.dataland.datalandmessagequeueutils.messages.data

/**
 * The payload for a data upload message
 */
data class QaPayload(
    val dataId: String,
    val bypassQa: Boolean,
)
