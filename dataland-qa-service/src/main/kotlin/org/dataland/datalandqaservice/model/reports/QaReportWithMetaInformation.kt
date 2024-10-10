package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * QA report and the associated meta information
 * @param report a QA report of the type QaReportType
 * @param metaInfo the associated meta information
 */
data class QaReportWithMetaInformation<QaReportType>(
    @field:JsonProperty(required = true)
    val metaInfo: QaReportMetaInformation,
    @field:JsonProperty(required = true)
    val report: QaReportType,
)
