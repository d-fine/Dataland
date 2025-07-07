package org.dataland.datalandqaservice.model.reports

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples

/**
 * A data point in a QA report.
 * @param T the type of the corrected data
 * @property comment a comment explaining the verdict
 * @property verdict the QA verdict for the data point
 * @property correctedData corrected data for the data point if applicable
 */
class QaReportDataPoint<T>(
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
    val correctedData: T?,
)
