package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict

/**
 * --- API model ---
 * Meta information associated to a QA report in the QA data storage
 * @param dataPointId unique identifier to identify the data point the report is associated with
 * @param dataPointType unique identifier to identify the data point type the report is associated with
 * @param qaReportId unique identifier of the QA report
 * @param reporterUserId the user ID of the user who requested the upload of this QA report
 * @param uploadTime is a timestamp for the upload of this QA report
 * @param active true iff the qa report is marked as active
 * @param comment a comment explaining the verdict
 * @param verdict the quality decision of this qa report
 * @param correctedData if rejected, contains suggested data corrections for the data point
 */
data class DataPointQaReport(
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.DATA_POINT_ID_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.DATA_POINT_ID_DESCRIPTION,
    )
    val dataPointId: String,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.DATA_POINT_TYPE_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.DATA_POINT_TYPE_EXAMPLE,
    )
    val dataPointType: String,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.QA_REPORT_ID_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.QA_REPORT_ID_EXAMPLE,
    )
    val qaReportId: String,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.REPORTER_USER_ID_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.REPORTER_USER_ID_EXAMPLE,
    )
    val reporterUserId: String?,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.QA_REPORT_UPLOAD_TIME_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.QA_REPORT_UPLOAD_TIME_EXAMPLE,
    )
    val uploadTime: Long,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.IS_REPORT_ACTIVE_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.IS_REPORT_ACTIVE_EXAMPLE,
    )
    val active: Boolean,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.QA_REPORT_COMMENT_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.QA_REPORT_COMMENT_EXAMPLE,
    )
    val comment: String,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.QA_REPORT_DATA_POINT_VERDICT_DESCRIPTION,
    )
    val verdict: QaReportDataPointVerdict,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.QA_REPORT_CORRECTED_DATA_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.QA_REPORT_CORRECTED_DATA_EXAMPLE,
    )
    val correctedData: String?,
)
