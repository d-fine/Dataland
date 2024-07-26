package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports

class QaReportWithMetaInformation<QaReportType>(
    val metaInformation: QaReportMetaInformation,
    val report: QaReportType,
)
