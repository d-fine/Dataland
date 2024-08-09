package org.dataland.datalandqaservice.model.reports

import io.swagger.v3.oas.annotations.media.Schema

/**
 * The verdict of a QA report data point
 */
@Schema(
    enumAsRef = true,
)
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
