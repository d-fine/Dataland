package org.dataland.datalandmessagequeueutils.messages.data

import java.util.UUID

/**
 * The payload for a data upload message
 */
data class DataUploadPayload(
    val dataId: UUID,
    val bypassQa: Boolean,
)
