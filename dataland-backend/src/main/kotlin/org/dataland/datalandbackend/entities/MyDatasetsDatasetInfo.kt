package org.dataland.datalandbackend.entities

import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.keycloakAdapter.auth.DatalandAuthentication

/**
 * The entity storing info for a dataset on the my datasets page
 */
data class MyDatasetsDatasetInfo(
    val companyId: String,
    val dataId: String,
    val companyName: String,
    val dataType: DataType,
    val reportingPeriod: String,
    val qualityStatus: QaStatus,
    var uploadTime: Long,
) {
    companion object {
        fun fromEntity(entity: MyDatasetsDatasetInfoEntity) = MyDatasetsDatasetInfo(
            companyId = entity.companyId,
            dataId = entity.dataId,
            companyName = entity.companyName,
            dataType = DataType.valueOf(entity.dataType),
            reportingPeriod = entity.reportingPeriod,
            qualityStatus = QaStatus.values().find { it.ordinal == entity.qualityStatus }!!,
            uploadTime = entity.uploadTime,
        )
    }
}
