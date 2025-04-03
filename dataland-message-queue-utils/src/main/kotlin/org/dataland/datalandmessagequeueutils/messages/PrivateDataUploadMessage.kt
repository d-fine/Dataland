package org.dataland.datalandmessagequeueutils.messages

/**
 * Message that is sent to the PRIVATE_REQUEST_RECEIVED exchange after a new data upload on Eurodat happened.
 * @param dataId dataId of new dataset
 * @param companyId ID of the company with which the new dataset is associated
 * @param reportingPeriod associated reporting period
 * @param actionType action to pursue in reaction to the sent message
 * @param documentHashes map of document hashes
 */
data class PrivateDataUploadMessage(
    val dataId: String,
    val companyId: String,
    val reportingPeriod: String,
    val actionType: String,
    val documentHashes: Map<String, String>,
)
