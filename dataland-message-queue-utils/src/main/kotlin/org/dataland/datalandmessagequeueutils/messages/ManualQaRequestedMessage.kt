package org.dataland.datalandmessagequeueutils.messages

/**
 * Message that is sent from the Internal Storage to the QA Service after data has been stored in database.
 */
data class ManualQaRequestedMessage(
    val resourceId: String,
    val bypassQa: Boolean?,
)
