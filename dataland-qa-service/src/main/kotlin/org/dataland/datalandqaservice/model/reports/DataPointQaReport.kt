package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports

import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict

/**
 * --- API model ---
 * Meta information associated to a QA report in the QA data storage
 * @param dataId unique identifier to identify the data the report is associated with
 * @param dataPointIdentifier unique identifier to identify the data point the report is associated with
 * @param qaReportId unique identifier of the QA report
 * @param reporterUserId the user ID of the user who requested the upload of this QA report
 * @param uploadTime is a timestamp for the upload of this QA report
 * @param active true iff the qa report is marked as active
 * @param comment a comment explaining the verdict
 * @param verdict the quality decision of this qa report
 * @param correctedData if rejected, contains suggested data corrections for the data point
 */
data class DataPointQaReport(
    val dataId: String,
    val dataPointIdentifier: String,
    val qaReportId: String,
    val reporterUserId: String?,
    val uploadTime: Long,
    val active: Boolean,
    val comment: String,
    val verdict: QaReportDataPointVerdict,
    val correctedData: String?,
)
