package org.dataland.e2etests.tests.communityManager

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.model.AccessStatus
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.communitymanager.openApiClient.model.SingleDataRequest
import org.dataland.e2etests.auth.GlobalAuth.withTechnicalUser
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.communityManager.generateRandomLei
import org.dataland.e2etests.utils.communityManager.generateRandomPermId
import org.dataland.e2etests.utils.communityManager.getIdForUploadedCompanyWithIdentifiers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QueryDataRequestsCountingTests {
    val apiAccessor = ApiAccessor()
    val jwtHelper = JwtAuthenticationHelper()
    private val api = apiAccessor.requestControllerApi

    private val companyIdA = getIdForUploadedCompanyWithIdentifiers(lei = generateRandomLei())
    private val companyIdB = getIdForUploadedCompanyWithIdentifiers(permId = generateRandomPermId())

    private val dataTypeGetNumberOfRequestsSfdr = RequestControllerApi.DataTypeGetNumberOfRequests.sfdr
    private val dataTypeGetNumberOfRequestsP2p = RequestControllerApi.DataTypeGetNumberOfRequests.p2p
    private val dataTypeGetNumberOfRequestsVsme = RequestControllerApi.DataTypeGetNumberOfRequests.vsme

    private fun postSingleDataRequest(
        companyId: String,
        dataType: SingleDataRequest.DataType,
        reportingPeriods: Set<String>,
    ) {
        api.postSingleDataRequest(
            SingleDataRequest(companyIdentifier = companyId, dataType = dataType, reportingPeriods = reportingPeriods),
        )
    }

    @BeforeAll
    fun postDataRequestsBeforeQueryTest() {
        withTechnicalUser(TechnicalUser.PremiumUser) {
            postSingleDataRequest(companyIdA, SingleDataRequest.DataType.vsme, setOf("2022", "2023"))
            postSingleDataRequest(companyIdB, SingleDataRequest.DataType.p2p, setOf("2023"))
        }
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
    }

    @Test
    fun `count requests with request status filters`() {
        assertEquals(
            2,
            api.getNumberOfRequests(
                datalandCompanyId = companyIdA,
                requestStatus = setOf(RequestStatus.Open, RequestStatus.Resolved),
            ),
        )
        assertEquals(
            0,
            api.getNumberOfRequests(datalandCompanyId = companyIdA, requestStatus = setOf(RequestStatus.Resolved)),
        )
        assertEquals(
            1,
            api.getNumberOfRequests(
                datalandCompanyId = companyIdB,
                requestStatus = setOf(RequestStatus.Open, RequestStatus.Resolved),
            ),
        )
        assertEquals(
            0,
            api.getNumberOfRequests(datalandCompanyId = companyIdB, requestStatus = setOf(RequestStatus.Resolved)),
        )
    }

    @Test
    fun `count requests with access status filters`() {
        assertEquals(
            0,
            api.getNumberOfRequests(datalandCompanyId = companyIdA, accessStatus = setOf(AccessStatus.Public)),
        )
        assertEquals(
            2,
            api.getNumberOfRequests(datalandCompanyId = companyIdA, accessStatus = setOf(AccessStatus.Pending)),
        )
        assertEquals(
            1,
            api.getNumberOfRequests(datalandCompanyId = companyIdB, accessStatus = setOf(AccessStatus.Public)),
        )
        assertEquals(
            0,
            api.getNumberOfRequests(datalandCompanyId = companyIdB, accessStatus = setOf(AccessStatus.Pending)),
        )
    }

    @Test
    fun `count requests by single data type for company A`() {
        assertEquals(2, api.getNumberOfRequests(datalandCompanyId = companyIdA))
        assertEquals(
            2,
            api.getNumberOfRequests(
                datalandCompanyId = companyIdA,
                dataType = listOf(dataTypeGetNumberOfRequestsVsme),
            ),
        )
        assertEquals(
            0,
            api.getNumberOfRequests(
                datalandCompanyId = companyIdA,
                dataType = listOf(dataTypeGetNumberOfRequestsP2p),
            ),
        )
    }

    @Test
    fun `count requests by multiple data type for company A`() {
        assertEquals(
            2,
            api.getNumberOfRequests(
                datalandCompanyId = companyIdA,
                dataType = listOf(dataTypeGetNumberOfRequestsP2p, dataTypeGetNumberOfRequestsVsme),
            ),
        )
        assertEquals(
            2,
            api.getNumberOfRequests(
                datalandCompanyId = companyIdA,
                dataType =
                    listOf(
                        dataTypeGetNumberOfRequestsP2p, dataTypeGetNumberOfRequestsVsme, dataTypeGetNumberOfRequestsSfdr,
                    ),
            ),
        )
    }

    @Test
    fun `count requests by data type for company B`() {
        assertEquals(1, api.getNumberOfRequests(datalandCompanyId = companyIdB))
        assertEquals(
            0,
            api.getNumberOfRequests(
                datalandCompanyId = companyIdB, dataType = listOf(dataTypeGetNumberOfRequestsVsme),
            ),
        )
        assertEquals(
            1,
            api.getNumberOfRequests(
                datalandCompanyId = companyIdB, dataType = listOf(dataTypeGetNumberOfRequestsP2p),
            ),
        )
        assertEquals(
            1,
            api.getNumberOfRequests(
                datalandCompanyId = companyIdB,
                dataType =
                    listOf(
                        dataTypeGetNumberOfRequestsP2p, dataTypeGetNumberOfRequestsVsme, dataTypeGetNumberOfRequestsSfdr,
                    ),
            ),
        )
    }

    @Test
    fun `count requests with reporting period 2022 and request status filter`() {
        assertEquals(1, api.getNumberOfRequests(datalandCompanyId = companyIdA, reportingPeriod = "2022"))
        assertEquals(
            1,
            api.getNumberOfRequests(
                datalandCompanyId = companyIdA,
                reportingPeriod = "2022",
                dataType = listOf(dataTypeGetNumberOfRequestsVsme),
            ),
        )
        assertEquals(
            0,
            api.getNumberOfRequests(
                datalandCompanyId = companyIdA,
                reportingPeriod = "2022",
                dataType = listOf(dataTypeGetNumberOfRequestsSfdr),
            ),
        )
    }

    @Test
    fun `count requests with reporting period 2023 and request status filter`() {
        assertEquals(1, api.getNumberOfRequests(datalandCompanyId = companyIdB, reportingPeriod = "2023"))
        assertEquals(
            1,
            api.getNumberOfRequests(
                datalandCompanyId = companyIdB,
                reportingPeriod = "2023",
                requestStatus = setOf(RequestStatus.Open),
            ),
        )
        assertEquals(
            1,
            api.getNumberOfRequests(
                datalandCompanyId = companyIdB,
                reportingPeriod = "2023",
                dataType = listOf(dataTypeGetNumberOfRequestsP2p),
                requestStatus = setOf(RequestStatus.Open),
            ),
        )
    }

    @Test
    fun `count requests filtered by user id`() {
        val requesterUserId = TechnicalUser.PremiumUser.technicalUserId

        assertTrue(api.getNumberOfRequests(userId = requesterUserId) >= 3)
        assertTrue(
            api.getNumberOfRequests(
                dataType = listOf(dataTypeGetNumberOfRequestsVsme),
                userId = requesterUserId,
            ) >= 2,
        )
        assertTrue(
            api.getNumberOfRequests(
                dataType = listOf(dataTypeGetNumberOfRequestsP2p),
                userId = requesterUserId,
            ) >= 1,
        )
        assertTrue(
            api.getNumberOfRequests(
                dataType = listOf(dataTypeGetNumberOfRequestsP2p, dataTypeGetNumberOfRequestsVsme),
                userId = requesterUserId,
            ) >= 3,
        )
        assertTrue(
            api.getNumberOfRequests(
                dataType =
                    listOf(
                        dataTypeGetNumberOfRequestsP2p, dataTypeGetNumberOfRequestsVsme, dataTypeGetNumberOfRequestsSfdr,
                    ),
                userId = requesterUserId,
            ) >= 3,
        )
    }

    @Test
    fun `count requests without company filter`() {
        assertTrue(api.getNumberOfRequests() >= 3)
        assertTrue(api.getNumberOfRequests(dataType = listOf(dataTypeGetNumberOfRequestsVsme)) >= 2)
        assertTrue(api.getNumberOfRequests(dataType = listOf(dataTypeGetNumberOfRequestsP2p)) >= 1)
        assertTrue(
            api.getNumberOfRequests(
                dataType =
                    listOf(
                        dataTypeGetNumberOfRequestsP2p, dataTypeGetNumberOfRequestsVsme,
                    ),
            ) >= 3,
        )
        assertTrue(
            api.getNumberOfRequests(
                dataType =
                    listOf(
                        dataTypeGetNumberOfRequestsP2p, dataTypeGetNumberOfRequestsVsme, dataTypeGetNumberOfRequestsSfdr,
                    ),
            ) >= 3,
        )
    }
}
