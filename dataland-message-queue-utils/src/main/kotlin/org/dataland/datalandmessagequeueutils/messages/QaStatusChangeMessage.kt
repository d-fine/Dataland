package org.dataland.datalandmessagequeueutils.messages

import org.dataland.datalandbackendutils.model.QaStatus

/**
 * Message that is sent to the data quality assured exchange after
 * a qa status to a dataId has changed.
 */
data class QAStatusChangeMessage(
    val changedQaStatusDataId: String,
    val updatedQaStatus: QaStatus,
    val currentlyActiveDataId: String,
)
