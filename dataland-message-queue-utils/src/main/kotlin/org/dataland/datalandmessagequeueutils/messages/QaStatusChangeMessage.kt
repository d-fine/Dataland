package org.dataland.datalandmessagequeueutils.messages

import org.dataland.datalandbackendutils.model.QaStatus

/**
 * Message that is sent to the data quality assured exchange after a QA status to a dataId has changed.
 * @param dataId dataId of dataset for which QA status changes
 * @param updatedQaStatus the newly assigned QA status
 * @param currentlyActiveDataId dataId of dataset which is going to set to "active" in the backend metadata in case that
 * the QA status changes from Accepted to Pending or Rejected for the dataset behind dataId
 */
data class QaStatusChangeMessage(
    val dataId: String,
    val updatedQaStatus: QaStatus,
    val currentlyActiveDataId: String?,
)
