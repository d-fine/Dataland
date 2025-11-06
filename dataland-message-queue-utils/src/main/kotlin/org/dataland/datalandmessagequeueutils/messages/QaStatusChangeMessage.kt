package org.dataland.datalandmessagequeueutils.messages

import org.dataland.datalandbackendutils.model.QaStatus

/**
 * Message that is sent to the data quality assured exchange after a QA status to a dataId has changed.
 * @param dataId dataId of dataset for which QA status changes
 * @param updatedQaStatus the newly assigned QA status
 * @param currentlyActiveDataId dataId of dataset which is going to set to "active" in the backend metadata in case that
 * the QA status changes from Accepted to Pending or Rejected for the dataset behind dataId
 * @param companyId companyId of the dataId
 * @param framework framework of the dataId
 * @param reportingPeriod reportingPeriod of the dataId
 * @param isUpdate if there was a different dataid before which is now overwritten
 */
data class QaStatusChangeMessage(
    val dataId: String,
    val updatedQaStatus: QaStatus,
    val currentlyActiveDataId: String?,
    val companyId: String,
    val framework: String,
    val reportingPeriod: String,
    val isUpdate: Boolean,
)
