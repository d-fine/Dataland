package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.QaServiceOpenApiDescriptionsAndExamples
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import java.util.UUID

/**
 * A data point in a QA report.
 * @param T the type of the corrected data
 * @property qaReportId unique identifier of the QA report
 * @property verdict the QA verdict for the data point
 * @property correctedData corrected data for the data point if applicable
 */
class QaReportDataPointWithId<T>(
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.QA_REPORT_COMMENT_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.QA_REPORT_COMMENT_EXAMPLE,
    )
    val qaReportId: UUID,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.QA_REPORT_DATA_POINT_VERDICT_DESCRIPTION,
    )
    val verdict: QaReportDataPointVerdict,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.QA_REPORT_CORRECTED_DATA_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.QA_REPORT_CORRECTED_DATA_EXAMPLE,
    )
    val correctedData: T?,
)
