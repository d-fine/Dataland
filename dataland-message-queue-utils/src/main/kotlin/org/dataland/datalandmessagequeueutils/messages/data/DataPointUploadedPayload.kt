package org.dataland.datalandmessagequeueutils.messages.data

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import org.dataland.datalandbackendutils.model.QaStatus

/**
 * The payload for a data upload message for a data point
 */
data class DataPointUploadedPayload(
    val dataPointId: String,
    val companyId: String,
    val companyName: String,
    val dataPointType: String,
    val reportingPeriod: String,
    val uploadTime: Long,
    val uploaderUserId: String,
    val initialQa: InitialQaStatus,
)

/**
 * Describe the method for acquiring the initial QA status for a new data point
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    value = [
        JsonSubTypes.Type(value = PresetQaStatus::class, name = "preset"),
        JsonSubTypes.Type(value = CopyQaStatusFromDataset::class, name = "copyFromDataset"),
    ],
)
sealed class InitialQaStatus

/**
 * Use a pre-determined QA status for the new data point
 */
@JsonTypeName("preset")
data class PresetQaStatus(
    val qaStatus: QaStatus,
    val qaComment: String?,
) : InitialQaStatus()

/**
 * Copy the last QA entry from a dataset to the new data point
 */
@JsonTypeName("copyFromDataset")
data class CopyQaStatusFromDataset(
    val datasetId: String,
) : InitialQaStatus()
