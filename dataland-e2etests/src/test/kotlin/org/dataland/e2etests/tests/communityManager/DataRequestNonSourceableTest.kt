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
    private val stringThatMatchesThePermIdRegex = System.currentTimeMillis().toString()
    private val dummyCompanyId = getIdForUploadedCompanyWithIdentifiers(permId = stringThatMatchesThePermIdRegex)
    private val dummyDataType = SingleDataRequest.DataType.eutaxonomyMinusNonMinusFinancials

    private val firstUserRequest2023 =
        SingleDataRequest(
            companyIdentifier = dummyCompanyId,
            dataType = dummyDataType,
            reportingPeriods = setOf("2023"),
            contacts = null,
            message = null,
        )
    private val firstUserRequest2024 =
        SingleDataRequest(
            companyIdentifier = dummyCompanyId,
            dataType = dummyDataType,
            reportingPeriods = setOf("2022"),
            contacts = null,
            message = null,
        )

    private val secondUserRequest2023 =
        SingleDataRequest(
            companyIdentifier = dummyCompanyId,
            dataType = dummyDataType,
            reportingPeriods = setOf("2023"),
            contacts = null,
            message = null,
        )

    private val nonSourceableInfoRequest2023 =
        NonSourceableInfo(
            companyId = dummyCompanyId,
            dataType = DataTypeEnum.eutaxonomyMinusNonMinusFinancials,
            reportingPeriod = "2023",
            isNonSourceable = true,
            reason = "This dataset is non-sourceable.",
        )

    private fun postTwoDataRequestForSameUserAndReturnRequestIds(): Pair<UUID, UUID> {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)

        var timestampBeforeSingleRequest = retrieveTimeAndWaitOneMillisecond()
        requestControllerApi.postSingleDataRequest(firstUserRequest2023)
        val storedFirstUserRequest2023List = getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)

        timestampBeforeSingleRequest = retrieveTimeAndWaitOneMillisecond()
        requestControllerApi.postSingleDataRequest(firstUserRequest2024)
        val storedFirstUserRequest2024List = getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)

        val requestIdFirstUserRequest2023 =
            UUID.fromString(storedFirstUserRequest2023List[0].dataRequestId)

        val requestIdFirstUserRequest2024 =
            UUID.fromString(storedFirstUserRequest2024List[0].dataRequestId)

        return Pair(requestIdFirstUserRequest2023, requestIdFirstUserRequest2024)
    }

    private fun postADataRequestAndReturnRequestId(): UUID {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)

        val timestampBeforeSingleRequest = retrieveTimeAndWaitOneMillisecond()
        requestControllerApi.postSingleDataRequest(secondUserRequest2023)
        val storedSecondUserRequest2023List = getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)

        val requestIdSecondUserRequest2023 =
            UUID.fromString(storedSecondUserRequest2023List[0].dataRequestId)

        return requestIdSecondUserRequest2023
    }

    private fun postNonSourceableInfo(nonSourceableInfo: NonSourceableInfo) {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        apiAccessor.metaDataControllerApi.postNonSourceabilityOfADataset(
            nonSourceableInfo = nonSourceableInfo,
        )
    }

    @Test
    fun `validate that only the requests corresponding to the nonSourceable dataset are patched`() {
        // Post requests for 2023 and 2024 as premium user.
        val requestIdsFirstUser = postTwoDataRequestForSameUserAndReturnRequestIds()
        val requestIdFirstUserRequest2023 = requestIdsFirstUser.first
        val requestIdFirstUserRequest2024 = requestIdsFirstUser.second

        // Post request for 2023 as admin.
        val requestIdSecondUserRequest2023 = postADataRequestAndReturnRequestId()

        postNonSourceableInfo(nonSourceableInfoRequest2023)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)

        val updatedFirstUserRequest2023 =
            requestControllerApi.getDataRequestById(requestIdFirstUserRequest2023)

        val updatedFirstUserRequest2024 =
            requestControllerApi.getDataRequestById(requestIdFirstUserRequest2024)

        val updatedSecondUserRequest2023 =
            requestControllerApi.getDataRequestById(requestIdSecondUserRequest2023)

        assertEquals(RequestStatus.NonSourceable, updatedFirstUserRequest2023.requestStatus)
        assertEquals(RequestStatus.Open, updatedFirstUserRequest2024.requestStatus)
        assertEquals(RequestStatus.NonSourceable, updatedSecondUserRequest2023.requestStatus)
    }

    @Test
    fun `validate that the get info on sourceability of a dataset endpoint is working`() {
        postNonSourceableInfo(nonSourceableInfoRequest2023)
        val receivedNonSourceableInfoList =
            apiAccessor.metaDataControllerApi.getInfoOnNonSourceabilityOfDatasets(
                companyId = nonSourceableInfoRequest2023.companyId,
                dataType = nonSourceableInfoRequest2023.dataType,
                reportingPeriod = nonSourceableInfoRequest2023.reportingPeriod,
            )

        assertEquals(1, receivedNonSourceableInfoList.size)
        val receivedNonSourceableInfo = receivedNonSourceableInfoList[0]

        assertEquals(nonSourceableInfoRequest2023.companyId, receivedNonSourceableInfo.companyId)
        assertEquals(nonSourceableInfoRequest2023.dataType, receivedNonSourceableInfo.dataType)
        assertEquals(nonSourceableInfoRequest2023.reportingPeriod, receivedNonSourceableInfo.reportingPeriod)
        assertEquals(nonSourceableInfoRequest2023.isNonSourceable, receivedNonSourceableInfo.isNonSourceable)
        assertEquals(nonSourceableInfoRequest2023.reason, receivedNonSourceableInfo.reason)
    }
}
