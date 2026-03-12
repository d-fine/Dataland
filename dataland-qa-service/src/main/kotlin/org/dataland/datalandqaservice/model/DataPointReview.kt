package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.QaServiceOpenApiDescriptionsAndExamples
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportDataPointWithReporter
import java.util.UUID

/**
 * API response DTO for per-data-point review information.
 *
 * This class is used to return detailed information about the QA reports submitted for a specific data point, including
 * which source was accepted (original data point, corrected data from a QA report, or a custom value), and
 * details about the accepted QA report if applicable.
 *
 * @property dataPointType the type identifier of the data point
 * @property dataPointId the ID of the original data point instance
 * @property qaReports the QA report data points submitted for this data point type
 * @property acceptedSource which source was accepted for this data point
 * @property companyIdOfAcceptedQaReport the company whose QA report was accepted, if applicable
 * @property customValue the custom value accepted for this data point, if applicable
 */
data class DataPointReview(
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.DATA_POINT_TYPE_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.DATA_POINT_TYPE_EXAMPLE,
    )
    val dataPointType: String,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.DATA_POINT_ID_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.DATA_POINT_ID_EXAMPLE,
    )
    val dataPointId: UUID?,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.QA_REPORTS_DESCRIPTION,
    )
    val qaReports: List<QaReportDataPointWithReporter>,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.ACCEPTED_SOURCE_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.ACCEPTED_SOURCE_EXAMPLE,
    )
    val acceptedSource: AcceptedDataPointSource?,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.ACCEPTED_REPORTER_USER_ID_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.ACCEPTED_REPORTER_USER_ID_EXAMPLE,
    )
    val reporterUserIdOfAcceptedQaReport: UUID?,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.QA_REPORT_COMPANY_ID_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.ACCEPTED_QA_REPORT_COMPANY_ID_DESCRIPTION,
    )
    val companyIdOfAcceptedQaReport: UUID?,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.DATA_REVIEW_CUSTOM_DATAPOINTS_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.DATA_REVIEW_CUSTOM_DATAPOINTS_EXAMPLE,
    )
    val customValue: String?,
)
