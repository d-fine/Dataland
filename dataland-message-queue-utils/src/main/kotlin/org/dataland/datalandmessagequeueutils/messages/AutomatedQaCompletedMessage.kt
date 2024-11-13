package org.dataland.datalandmessagequeueutils.messages

import org.dataland.datalandbackendutils.model.QaStatus

/**
 * Message that is sent from the Automated QA Service to the manual QA service after automated quality assurance of
 * provided data has been completed.
 */
data class AutomatedQaCompletedMessage(
    val resourceId: String,
    val qaStatus: QaStatus?,
    val reviewerId: String,
    val bypassQa: Boolean,
    val comment: String?,
)
