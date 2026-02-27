package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportDataPointWithReporterDetails
import java.util.UUID

/**
 * API response DTO for per-data-point review information.
 * @property dataPointType the type identifier of the data point
 * @property dataPointId the ID of the original data point instance
 * @property qaReports the QA report data points submitted for this data point type
 * @property acceptedSource which source was accepted for this data point
 * @property companyIdOfAcceptedQaReport the company whose QA report was accepted, if applicable
 * @property customValue the custom value accepted for this data point, if applicable
 */
data class DataPointReviewDetails(
    val dataPointType: String,
    val dataPointId: UUID?,
    val qaReports: List<QaReportDataPointWithReporterDetails>,
    val acceptedSource: AcceptedDataPointSource?,
    val companyIdOfAcceptedQaReport: UUID?,
    val customValue: String?,
)
