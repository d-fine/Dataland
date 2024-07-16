package org.dataland.e2etests.tests.communityManager

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.communitymanager.openApiClient.model.SingleDataRequest
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.communityManager.generateRandomLei
import org.dataland.e2etests.utils.communityManager.generateRandomPermId
import org.dataland.e2etests.utils.communityManager.getIdForUploadedCompanyWithIdentifiers
import org.dataland.e2etests.utils.communityManager.retrieveTimeAndWaitOneMillisecond
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QueryDataRequestsTest {
    val apiAccessor = ApiAccessor()
    val jwtHelper = JwtAuthenticationHelper()
    private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)

    private val companyIdA = getIdForUploadedCompanyWithIdentifiers(lei = generateRandomLei())
    private val companyIdB = getIdForUploadedCompanyWithIdentifiers(permId = generateRandomPermId())
    private val timestampBeforePost = retrieveTimeAndWaitOneMillisecond()

    @BeforeAll
    fun postDataRequestsBeforeQueryTest() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        requestControllerApi.postSingleDataRequest(
            SingleDataRequest(
                companyIdentifier = companyIdA,
                dataType = SingleDataRequest.DataType.lksg,
                reportingPeriods = setOf("2022", "2023"),
            ),
        )
        requestControllerApi.postSingleDataRequest(
            SingleDataRequest(
                companyIdentifier = companyIdB,
                dataType = SingleDataRequest.DataType.p2p,
                reportingPeriods = setOf("2023"),
            ),
        )
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
    }

    @Test
    fun `query data requests with no filters and assert that the expected results are being retrieved`() {
        val storedDataRequests = requestControllerApi.getDataRequests().filter {
            it.creationTimestamp > timestampBeforePost
        }
        assertEquals(3, storedDataRequests.size)
    }

    @Test
    fun `query data requests with data type filter and assert that the expected results are being retrieved`() {
        val vsmeDataRequests = requestControllerApi.getDataRequests(
            dataType = RequestControllerApi.DataTypeGetDataRequests.vsme,
        ).filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(0, vsmeDataRequests.size)

        val p2pDataRequests = requestControllerApi.getDataRequests(
            dataType = RequestControllerApi.DataTypeGetDataRequests.p2p,
        ).filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(1, p2pDataRequests.size)
        assertEquals(DataTypeEnum.p2p.value, p2pDataRequests.first().dataType)

        val lksgDataRequests = requestControllerApi.getDataRequests(
            dataType = RequestControllerApi.DataTypeGetDataRequests.lksg,
        ).filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(2, lksgDataRequests.size)
        lksgDataRequests.forEach { assertEquals(DataTypeEnum.lksg.value, it.dataType) }
    }

    @Test
    fun `query data requests with reporting period filter and assert that the expected results are being retrieved`() {
        val dataRequestsFor2021 = requestControllerApi.getDataRequests(reportingPeriod = "2021").filter {
            it.creationTimestamp > timestampBeforePost
        }
        assertEquals(0, dataRequestsFor2021.size)

        val dataRequestsFor2022 = requestControllerApi.getDataRequests(reportingPeriod = "2022").filter {
            it.creationTimestamp > timestampBeforePost
        }
        assertEquals(1, dataRequestsFor2022.size)
        assertEquals("2022", dataRequestsFor2022.first().reportingPeriod)

        val dataRequestsFor2023 = requestControllerApi.getDataRequests(reportingPeriod = "2023").filter {
            it.creationTimestamp > timestampBeforePost
        }
        assertEquals(2, dataRequestsFor2023.size)
        dataRequestsFor2023.forEach { assertEquals("2023", it.reportingPeriod) }
    }

    @Test
    fun `query data requests with request status filter and assert that the expected results are being retrieved`() {
        val dataRequestIdB = UUID.fromString(
            requestControllerApi.getDataRequests().filter {
                it.creationTimestamp > timestampBeforePost
            }.first {
                it.datalandCompanyId == companyIdB
            }.dataRequestId,
        )

        val closedDataRequests = requestControllerApi.getDataRequests(requestStatus = RequestStatus.Resolved).filter {
            it.creationTimestamp > timestampBeforePost
        }
        assertEquals(0, closedDataRequests.size)

        val storedDataRequestB = requestControllerApi.getDataRequestById(dataRequestIdB)
        assertEquals(DataTypeEnum.p2p.value, storedDataRequestB.dataType)
        assertEquals("2023", storedDataRequestB.reportingPeriod)

        requestControllerApi.patchDataRequest(dataRequestIdB, RequestStatus.Answered)

        val answeredDataRequests = requestControllerApi.getDataRequests(requestStatus = RequestStatus.Answered).filter {
            it.creationTimestamp > timestampBeforePost
        }
        assertEquals(1, answeredDataRequests.size)
        assertEquals(companyIdB, answeredDataRequests.first().datalandCompanyId)
        assertEquals(RequestStatus.Answered, answeredDataRequests.first().requestStatus)
    }

    @Test
    fun `query data requests with company id filter and assert that the expected results are being retrieved`() {
        val storedDataRequestsForRandomCompanyId = requestControllerApi.getDataRequests(
            datalandCompanyId = UUID.randomUUID().toString(),
        ).filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(0, storedDataRequestsForRandomCompanyId.size)

        val storedDataRequestsForCompanyB = requestControllerApi.getDataRequests(datalandCompanyId = companyIdB)
            .filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(1, storedDataRequestsForCompanyB.size)
        assertEquals(companyIdB, storedDataRequestsForCompanyB.first().datalandCompanyId)

        val storedDataRequestsForCompanyA = requestControllerApi.getDataRequests(datalandCompanyId = companyIdA)
            .filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(2, storedDataRequestsForCompanyA.size)
        storedDataRequestsForCompanyA.forEach { assertEquals(companyIdA, it.datalandCompanyId) }
    }

    @Test
    fun `query data requests with user id filter and assert that the expected results are being retrieved`() {
        val dataRequestsByAdmin = requestControllerApi.getDataRequests(
            userId = TechnicalUser.Admin.technicalUserId,
        ).filter {
            it.creationTimestamp > timestampBeforePost
        }
        assertEquals(0, dataRequestsByAdmin.size)

        val dataRequestsByPremiumUser = requestControllerApi.getDataRequests(
            userId = TechnicalUser.PremiumUser.technicalUserId,
        ).filter {
            it.creationTimestamp > timestampBeforePost
        }
        assertEquals(3, dataRequestsByPremiumUser.size)
        dataRequestsByPremiumUser.forEach { assertEquals(TechnicalUser.PremiumUser.technicalUserId, it.userId) }
    }
}
