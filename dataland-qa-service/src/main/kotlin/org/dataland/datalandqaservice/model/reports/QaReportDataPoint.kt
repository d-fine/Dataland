package org.dataland.datalandqaservice.model.reports

class QaReportDataPoint<T>(
    val comment: String,
    val verdict: QaReportDataPointVerdict,
    val correctedData: T,
)
