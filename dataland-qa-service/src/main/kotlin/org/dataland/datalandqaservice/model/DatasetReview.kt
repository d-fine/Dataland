package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import java.util.UUID

/**
 * API model for the dataset review objects returned.
 */
@SuppressWarnings("LongParameterList")
class DatasetReview(
    val dataSetReviewId: UUID,
    val datasetId: UUID,
    val companyId: UUID,
    val dataType: String,
    val reportingPeriod: String,
    var status: QaStatus = QaStatus.Pending,
    var reviewerUserId: String,
    var preapprovedDataPointIds: Set<UUID> = emptySet(),
    var qaReports: MutableSet<DataPointQaReportEntity>,
    var approvedQaReportIds: Set<UUID> = emptySet(),
    var approvedDataPointIds: Set<UUID> = emptySet(),
    var approvedCustomDataPointIds: Map<UUID, String> = emptyMap(),
)
