package org.dataland.datalandbackend.model.datapoints

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackendutils.interfaces.DataPointInstance
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples
import java.time.Instant

/**
 * --- API model ---
 * Class for defining the fields needed by the Data Point Manager to handle data storage
 * @param dataPoint the content of the data point as a JSON string
 * @param dataPointType which data point the provided content is associated to
 * @param companyId identifies the company for which the data point is provided
 * @param reportingPeriod marks a period - e.g. a year or a specific quarter in a year - for which the data is valid
 */

data class UploadedDataPoint(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.DATA_POINT_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.DATA_POINT_EXAMPLE,
    )
    override val dataPoint: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.DATA_POINT_TYPE_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.DATA_POINT_TYPE_EXAMPLE,
    )
    override val dataPointType: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    override val companyId: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    override val reportingPeriod: String,
) : DataPointInstance {
    /**
     * Method to convert the uploaded data point to a data point meta information entity
     * @param dataPointId the id of the data point
     * @param uploaderUserId the user id of the user who uploaded the data point
     * @param uploadTime the time of the upload (default is the current time)
     * @param currentlyActive whether the data point is currently active (default is false)
     * @param qaStatus the QA status of the data point (default is Pending)
     */
    fun toDataPointMetaInformationEntity(
        dataPointId: String,
        uploaderUserId: String,
        uploadTime: Long = Instant.now().toEpochMilli(),
        currentlyActive: Boolean? = null,
        qaStatus: QaStatus = QaStatus.Pending,
    ): DataPointMetaInformationEntity =
        DataPointMetaInformationEntity(
            dataPointId = dataPointId,
            companyId = companyId,
            dataPointType = dataPointType,
            reportingPeriod = reportingPeriod,
            uploaderUserId = uploaderUserId,
            uploadTime = uploadTime,
            currentlyActive = currentlyActive,
            qaStatus = qaStatus,
        )
}
