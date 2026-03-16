package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.QaServiceOpenApiDescriptionsAndExamples
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict

/**
 * API response DTO for a QA report data point with reporter details.
 *
 * This class is used to return information about each QA report, including the verdict, any corrected data, and details
 * about the reporter (user ID and company ID).
 *
 * @property qaReportId the ID of the QA report this data point belongs to
 * @property verdict the QA verdict for the data point
 * @property correctedData corrected data for the data point if applicable
 * @property reporterUserId the ID of the user who reported this data point
 */
data class QaReportDataPointWithReporter(
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.QA_REPORT_ID_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.QA_REPORT_ID_EXAMPLE,
    )
    val qaReportId: String,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.QA_REPORT_DATA_POINT_VERDICT_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.QA_REPORT_VERDICT_EXAMPLE,
    )
    val verdict: QaReportDataPointVerdict,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.QA_REPORT_CORRECTED_DATA_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.QA_REPORT_CORRECTED_DATA_EXAMPLE,
    )
    val correctedData: String?,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.REVIEWER_USER_ID_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.REVIEWER_USER_ID_EXAMPLE,
    )
    val reporterUserId: String,
)
