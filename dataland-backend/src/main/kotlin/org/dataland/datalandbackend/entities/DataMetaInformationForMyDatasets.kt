package org.dataland.datalandbackend.entities

import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackendutils.model.QaStatus

/**
 * A variation of the DataMetaInformation augmented by the information needed for display on the "My Datasets" page
 */
data class DataMetaInformationForMyDatasets(
    val companyId: String,
    val dataId: String,
    val companyName: String,
    val dataType: DataType,
    val reportingPeriod: String,
    val qualityStatus: QaStatus,
    val currentlyActive: Boolean,
    var uploadTime: Long,
) {
    companion object {
        fun fromEntity(entity: DatasetMetaInfoEntityForMyDatasets) = DataMetaInformationForMyDatasets(
            companyId = entity.companyId,
            dataId = entity.dataId,
            companyName = entity.companyName,
            dataType = DataType.valueOf(entity.dataType),
            reportingPeriod = entity.reportingPeriod,
            qualityStatus = QaStatus.values().find { it.ordinal == entity.qualityStatus }!!,
            currentlyActive = entity.currentlyActive,
            uploadTime = entity.uploadTime,
        )
    }
}
