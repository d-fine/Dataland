package org.dataland.datalandmessagequeueutils.messages

import org.dataland.datalandbackendutils.model.QaStatus

/**
 * Message that is sent to the data quality assured exchange after
 * quality assurance of provided data has been completed.
 */
data class QaCompletedMessage(
    val identifier: String,
    val validationResult: QaStatus,
    val reviewerId: String,
    val message: String? = null,
)
