package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportDataPointWithId

/**
 * Data class which includes relevant information for the review process.
 * @param T the type of the custom value
 * @property dataPointTypeId the identifier for the type of the data point
 * @property dataPointId the identifier for the specific data point
 * @property qaReport the QA report data point associated with this review overview
 * @property acceptedSource the source from which the accepted value for this data point was derived
 * @property customValue a custom value for this data point if applicable
 */
data class DataPointReviewOverview(
    val dataPointTypeId: String,
    val dataPointId: String,
    val qaReport: QaReportDataPointWithId,
    val acceptedSource: AcceptedDataPointSource,
    val customValue: String?,
)
