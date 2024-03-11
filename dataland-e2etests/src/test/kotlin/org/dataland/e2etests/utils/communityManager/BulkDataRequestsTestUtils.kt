package org.dataland.e2etests.utils.communityManager

import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.communitymanager.openApiClient.model.StoredDataRequest
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.patchDataRequestAndAssertNewStatusAndLastModifiedUpdated
import org.junit.jupiter.api.Assertions
import java.util.*

fun retrieveDataRequestIdForReportingPeriodAndUpdateStatus(
    dataRequests: List<StoredDataRequest>,
    reportingPeriod: String,
    newStatus: RequestStatus,
) {
    val dataRequestsForReportingPeriod = dataRequests.filter { it.reportingPeriod == reportingPeriod }
    Assertions.assertEquals(
        1,
        dataRequestsForReportingPeriod.size,
        "There is more than one data request for reporting period $reportingPeriod although it shouldn't.",
    )
    val dataRequestId = UUID.fromString(dataRequestsForReportingPeriod[0].dataRequestId)
    jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
    patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(dataRequestId, newStatus)
}
