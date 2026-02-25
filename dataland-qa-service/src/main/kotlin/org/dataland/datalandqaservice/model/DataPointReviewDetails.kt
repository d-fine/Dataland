package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

import jakarta.persistence.ElementCollection
import jakarta.persistence.Embeddable
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportDataPointWithReporterDetails
import java.util.UUID

/**
 * Data class which includes relevant information for the review process.
 * @property dataPointTypeId the identifier for the type of the data point
 * @property dataPointId the identifier for the specific data point
 * @property qaReport the QA report data point associated with this review overview
 * @property acceptedSource the source from which the accepted value for this data point was derived
 * @property customValue a custom value for this data point if applicable
 */
@Embeddable
data class DataPointReviewDetails(
    val dataPointTypeId: UUID,
    val dataPointId: UUID,
    @ElementCollection
    val qaReport: List<QaReportDataPointWithReporterDetails>,
    val acceptedSource: AcceptedDataPointSource,
    val companyIdOfAcceptedQaReport: UUID?,
    val customValue: String?,
)
