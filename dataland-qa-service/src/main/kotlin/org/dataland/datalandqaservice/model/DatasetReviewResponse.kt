package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

/**
 * API model for the dataset review objects returned.
 */
data class DatasetReviewResponse(
    val dataSetReviewId: String,
    val datasetId: String,
    val companyId: String,
    val dataType: String,
    val reportingPeriod: String,
    var reviewState: DatasetReviewState,
    var reviewerUserId: String?,
    var reviewerUserName: String?,
    var preapprovedDataPointIds: Set<String>,
    var qaReports: Set<String>,
    var approvedQaReportIds: Map<String, String>,
    var approvedDataPointIds: Map<String, String>,
    var approvedCustomDataPointIds: Map<String, String>,
)
