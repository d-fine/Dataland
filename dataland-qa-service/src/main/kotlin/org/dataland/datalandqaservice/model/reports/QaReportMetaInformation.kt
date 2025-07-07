package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * Meta information associated to a QA report in the QA data storage
 * @param dataId unique identifier to identify the data the report  is associated with
 * @param qaReportId unique identifier of the QA report
 * @param reporterUserId the user ID of the user who requested the upload of this QA report
 * @param uploadTime is a timestamp for the upload of this QA report
 */
data class QaReportMetaInformation(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.DATA_ID_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.DATA_ID_EXAMPLE,
    )
    val dataId: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.DATA_TYPE_EXAMPLE,
    )
    val dataType: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.QA_REPORT_ID_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.QA_REPORT_ID_EXAMPLE,
    )
    val qaReportId: String,
    @field:JsonProperty()
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.REPORTER_USER_ID_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.REPORTER_USER_ID_DESCRIPTION,
    )
    val reporterUserId: String?,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.QA_REPORT_UPLOAD_TIME_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.QA_REPORT_UPLOAD_TIME_EXAMPLE,
    )
    val uploadTime: Long,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.IS_REPORT_ACTIVE_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.IS_REPORT_ACTIVE_EXAMPLE,
    )
    val active: Boolean,
)
