package org.dataland.e2etests.tests.communityManager

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.communitymanager.openApiClient.model.SingleDataRequest
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.NonSourceableInfo
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.communityManager.getIdForUploadedCompanyWithIdentifiers
import org.dataland.e2etests.utils.communityManager.getNewlyStoredRequestsAfterTimestamp
import org.dataland.e2etests.utils.communityManager.retrieveTimeAndWaitOneMillisecond
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataRequestNonSourceableTest {
    val apiAccessor = ApiAccessor()
    val jwtHelper = JwtAuthenticationHelper()
    private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)
    val stringThatMatchesThePermIdRegex = System.currentTimeMillis().toString()
    val companyId = getIdForUploadedCompanyWithIdentifiers(permId = stringThatMatchesThePermIdRegex)
    val reportingPeriodOne = setOf("2023")
    val reportingPeriodTwo = setOf("2022")
    val dataType = SingleDataRequest.DataType.eutaxonomyMinusNonMinusFinancials
    val singleDataRequestFirstUserSameRequest =
        SingleDataRequest(
            companyIdentifier = stringThatMatchesThePermIdRegex,
            dataType = dataType,
            reportingPeriods = reportingPeriodOne,
            contacts = setOf("simpleString@example.com"),
            message = "This is the request from the first user that should be unsourceable.",
        )
    val singleDataRequestFirstUserOtherRequest =
        SingleDataRequest(
            companyIdentifier = stringThatMatchesThePermIdRegex,
            dataType = dataType,
            reportingPeriods = reportingPeriodTwo,
            contacts = setOf("simpleString@example.com"),
            message = "This is the request from the first user that should not be unsourceable.",
        )

    val singleDataRequestSecondUserSameRequest =
        SingleDataRequest(
            companyIdentifier = stringThatMatchesThePermIdRegex,
            dataType = dataType,
            reportingPeriods = reportingPeriodOne,
            contacts = setOf("someContact@example.com"),
            message = "This is the request from the second user that should be unsourceable.",
        )

    @Test
    fun `post data requests from different users and check if correct requests are set to nonSourceable`() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        var timestampBeforeSingleRequest = retrieveTimeAndWaitOneMillisecond()
        requestControllerApi.postSingleDataRequest(singleDataRequestFirstUserSameRequest)
        val storedSingleDataRequestFirstUserSameRequest = getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)

        timestampBeforeSingleRequest = retrieveTimeAndWaitOneMillisecond()
        requestControllerApi.postSingleDataRequest(singleDataRequestFirstUserOtherRequest)
        val storedSingleDataRequestFirstUserOtherRequest = getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        timestampBeforeSingleRequest = retrieveTimeAndWaitOneMillisecond()
        requestControllerApi.postSingleDataRequest(singleDataRequestSecondUserSameRequest)
        val storedSingleDataRequestSecondUserSameRequest = getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)

        assertEquals(RequestStatus.Open, storedSingleDataRequestFirstUserSameRequest[0].requestStatus)
        assertEquals(RequestStatus.Open, storedSingleDataRequestSecondUserSameRequest[0].requestStatus)

        val nonSourceableInfo =
            NonSourceableInfo(
                companyId,
                DataTypeEnum.eutaxonomyMinusNonMinusFinancials,
                "2023",
                true,
                "This is a test so I need nonSourceable data.",
            )

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        apiAccessor.metaDataControllerApi.postNonSourceabilityOfADataset(
            nonSourceableInfo = nonSourceableInfo,
        )
// insert test of get endpoint

        val requestIdOfstoredSingleDataRequestFirstUserSameRequest =
            UUID
                .fromString(storedSingleDataRequestFirstUserSameRequest[0].dataRequestId)
        val requestIdOfstoredSingleDataRequestSecondUserSameRequest =
            UUID
                .fromString(storedSingleDataRequestSecondUserSameRequest[0].dataRequestId)
        val requestIdOfStoredSingleDataRequestFirstUserOtherRequest =
            UUID
                .fromString(storedSingleDataRequestFirstUserOtherRequest[0].dataRequestId)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)

        val updatedstoredSingleDataRequestFirstUserSameRequest =
            requestControllerApi
                .getDataRequestById(requestIdOfstoredSingleDataRequestFirstUserSameRequest)
        val updatedstoredSingleDataRequestSecondUserSameRequest =
            requestControllerApi
                .getDataRequestById(requestIdOfstoredSingleDataRequestSecondUserSameRequest)
        val updatedstoredSingleDataRequestFirstUserOtherRequest =
            requestControllerApi
                .getDataRequestById(requestIdOfStoredSingleDataRequestFirstUserOtherRequest)

        assertEquals(RequestStatus.NonSourceable, updatedstoredSingleDataRequestFirstUserSameRequest.requestStatus)
        assertEquals(RequestStatus.NonSourceable, updatedstoredSingleDataRequestSecondUserSameRequest.requestStatus)
        assertEquals(RequestStatus.Open, updatedstoredSingleDataRequestFirstUserOtherRequest.requestStatus)
    }
}
