package org.dataland.datalandbackend.entities

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples

/**
 * A variation of the DataMetaInformation augmented by the information needed for display on the "My Datasets" page
 */
data class DataMetaInformationForMyDatasets(
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val companyId: String,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.DATA_ID_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.DATA_ID_EXAMPLE,
    )
    val dataId: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_NAME_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_NAME_EXAMPLE,
    )
    val companyName: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
    )
    val dataType: DataType,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    val reportingPeriod: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.QA_STATUS_DESCRIPTION,
    )
    val qualityStatus: QaStatus,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.CURRENTLY_ACTIVE_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.CURRENTLY_ACTIVE_EXAMPLE,
    )
    val currentlyActive: Boolean?,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.UPLOAD_TIME_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.UPLOAD_TIME_EXAMPLE,
    )
    var uploadTime: Long,
) {
    companion object {
        /**
         * A function to construct a DatametaInformationForMyDatasets object from a entity object
         */
        fun fromDatasetMetaInfoEntityForMyDatasets(entity: DatasetMetaInfoEntityForMyDatasets) =
            DataMetaInformationForMyDatasets(
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
