package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import java.util.UUID

/**
 * API model for the dataset review objects returned.
 */
data class DatasetReview(
    val dataSetReviewId: UUID,
    val datasetId: UUID,
    val companyId: UUID,
    val dataType: String,
    val reportingPeriod: String,
    var status: DatasetReviewState,
    var reviewerUserId: UUID,
    var preapprovedDataPointIds: Set<UUID>,
    var qaReports: MutableSet<DataPointQaReportEntity>,
    var approvedQaReportIds: MutableMap<String, UUID>,
    var approvedDataPointIds: MutableMap<String, UUID>,
    var approvedCustomDataPointIds: MutableMap<String, String>,
)
