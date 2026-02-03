package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportIdWithUploaderCompanyId

/**
 * API model for the dataset review objects returned.
 */
data class DatasetReviewResponse(
    val dataSetReviewId: String,
    val datasetId: String,
    val companyId: String,
    val dataType: String,
    val reportingPeriod: String,
    var status: DatasetReviewState,
    var reviewerUserId: String,
    var preapprovedDataPointIds: Set<String>,
    var qaReports: Set<QaReportIdWithUploaderCompanyId>,
    var approvedQaReportIds: Map<String, String>,
    var approvedDataPointIds: Map<String, String>,
    var approvedCustomDataPointIds: Map<String, String>,
)
