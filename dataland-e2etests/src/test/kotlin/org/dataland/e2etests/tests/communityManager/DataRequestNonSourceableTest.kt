package org.dataland.e2etests.tests.communityManager

import org.awaitility.Awaitility
import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.communitymanager.openApiClient.model.SingleDataRequest
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.SourceabilityInfo
import org.dataland.datalandbackend.openApiClient.model.SourceabilityInfoResponse
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
import java.util.concurrent.TimeUnit

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
            notifyMeImmediately = false,
        )
    private val firstUserRequest2024 =
        SingleDataRequest(
            companyIdentifier = dummyCompanyId,
            dataType = dummyDataType,
            reportingPeriods = setOf("2022"),
            contacts = null,
            message = null,
            notifyMeImmediately = false,
        )

    private val secondUserRequest2023 =
        SingleDataRequest(
            companyIdentifier = dummyCompanyId,
            dataType = dummyDataType,
            reportingPeriods = setOf("2023"),
            contacts = null,
            message = null,
            notifyMeImmediately = false,
        )

    private val sourceabilityInfoRequest2023 =
        SourceabilityInfo(
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

    private fun postSourceabilityInfo(sourceabilityInfo: SourceabilityInfo) {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        apiAccessor.metaDataControllerApi.postNonSourceabilityOfADataset(
            sourceabilityInfo = sourceabilityInfo,
        )
    }

    private fun awaitUntilAsserted(operation: () -> Any) =
        Awaitility.await().atMost(2000, TimeUnit.MILLISECONDS).pollDelay(500, TimeUnit.MILLISECONDS).untilAsserted {
            operation()
        }

    @Test
    fun `validate that only the requests corresponding to the nonSourceable dataset are patched`() {
        // Post requests for 2023 and 2024 as premium user.
        val requestIdsFirstUser = postTwoDataRequestForSameUserAndReturnRequestIds()
        val requestIdFirstUserRequest2023 = requestIdsFirstUser.first
        val requestIdFirstUserRequest2024 = requestIdsFirstUser.second

        // Post request for 2023 as admin.
        val requestIdSecondUserRequest2023 = postADataRequestAndReturnRequestId()

        postSourceabilityInfo(sourceabilityInfoRequest2023)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)

        awaitUntilAsserted {
            val updatedFirstUserRequest2023 =
                requestControllerApi.getDataRequestById(requestIdFirstUserRequest2023)
            assertEquals(RequestStatus.NonSourceable, updatedFirstUserRequest2023.requestStatus)
        }
        awaitUntilAsserted {
            val updatedFirstUserRequest2024 =
                requestControllerApi.getDataRequestById(requestIdFirstUserRequest2024)
            assertEquals(RequestStatus.Open, updatedFirstUserRequest2024.requestStatus)
        }
        awaitUntilAsserted {
            val updatedSecondUserRequest2023 =
                requestControllerApi.getDataRequestById(requestIdSecondUserRequest2023)
            assertEquals(RequestStatus.NonSourceable, updatedSecondUserRequest2023.requestStatus)
        }
    }

    @Test
    fun `validate that the get info on sourceability of a dataset endpoint is working`() {
        postSourceabilityInfo(sourceabilityInfoRequest2023)
        var receivedSourceabilityInfoList = listOf<SourceabilityInfoResponse>()

        awaitUntilAsserted {
            receivedSourceabilityInfoList =
                apiAccessor.metaDataControllerApi.getInfoOnNonSourceabilityOfDatasets(
                    companyId = sourceabilityInfoRequest2023.companyId,
                    dataType = sourceabilityInfoRequest2023.dataType,
                    reportingPeriod = sourceabilityInfoRequest2023.reportingPeriod,
                )
            assertEquals(1, receivedSourceabilityInfoList.size)
        }

        val receivedSourceabilityInfo = receivedSourceabilityInfoList[0]

        assertEquals(sourceabilityInfoRequest2023.companyId, receivedSourceabilityInfo.companyId)
        assertEquals(sourceabilityInfoRequest2023.dataType, receivedSourceabilityInfo.dataType)
        assertEquals(sourceabilityInfoRequest2023.reportingPeriod, receivedSourceabilityInfo.reportingPeriod)
        assertEquals(sourceabilityInfoRequest2023.isNonSourceable, receivedSourceabilityInfo.isNonSourceable)
        assertEquals(sourceabilityInfoRequest2023.reason, receivedSourceabilityInfo.reason)
    }
}
