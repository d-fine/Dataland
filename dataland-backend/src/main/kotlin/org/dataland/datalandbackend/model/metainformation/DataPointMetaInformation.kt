package org.dataland.datalandbackend.model.metainformation

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.interfaces.DataPointDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * Meta information associated to data in the data store
 * @param dataPointId unique identifier to identify the data in the data store
 * @param dataPointType unique identifier for the data point itself
 * @param companyId unique identifier to identify the company the data is associated with
 * @param reportingPeriod marks a period - e.g. a year or a specific quarter in a year - for which the data is valid
 * @param uploaderUserId the user ID of the user who uploaded the data point
 * @param uploadTime is a timestamp for the upload of this data point
 */
data class DataPointMetaInformation(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.DATA_POINT_ID_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.DATA_POINT_ID_EXAMPLE,
    )
    val dataPointId: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.DATA_POINT_TYPE_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.DATA_POINT_TYPE_EXAMPLE,
    )
    override val dataPointType: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    override val companyId: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    override val reportingPeriod: String,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.UPLOADER_USER_ID_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.UPLOADER_USER_ID_EXAMPLE,
    )
    val uploaderUserId: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.UPLOAD_TIME_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.UPLOAD_TIME_EXAMPLE,
    )
    val uploadTime: Long,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.CURRENTLY_ACTIVE_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.CURRENTLY_ACTIVE_EXAMPLE,
    )
    val currentlyActive: Boolean,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.QA_STATUS_DESCRIPTION,
    )
    val qaStatus: QaStatus,
) : DataPointDimensions
