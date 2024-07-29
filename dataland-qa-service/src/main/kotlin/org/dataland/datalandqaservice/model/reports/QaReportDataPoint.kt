package org.dataland.datalandqaservice.model.reports

/**
 * A data point in a QA report.
 * @param T the type of the corrected data
 * @property comment a comment explaining the verdict
 * @property verdict the QA verdict for the data point
 * @property correctedData corrected data for the data point if applicable
 */
class QaReportDataPoint<T>(
    val comment: String,
    val verdict: QaReportDataPointVerdict,
    val correctedData: T,
)
