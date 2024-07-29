package org.dataland.datalandqaservice.model.reports

/**
 * The verdict of a QA report data point
 */
enum class QaReportDataPointVerdict {
    /**
     * The data point was accepted
     */
    QaAccepted,

    /**
     * The data point was rejected
     */
    QaRejected,

    /**
     * A QA verdict could not be given as the data was inconclusive
     */
    QaInconclusive,

    /**
     * QA was not attempted (e.g., because the type of data was not supported)
     */
    QaNotAttempted,
}
