package org.dataland.e2etests.tests.communityManager

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.communitymanager.openApiClient.model.SingleDataRequest
import org.dataland.communitymanager.openApiClient.model.StoredDataRequest
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.PREMIUM_USER_ID
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.generateCompaniesWithOneRandomValueForEachIdentifierType
import org.dataland.e2etests.utils.generateRandomIsin
import org.dataland.e2etests.utils.getUniqueDatalandCompanyIdForIdentifierValue
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QueryDataRequestsTest {
    val apiAccessor = ApiAccessor()
    val jwtHelper = JwtAuthenticationHelper()
    private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)

    fun authenticateAsPremiumUser() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
    }

    private lateinit var permIdOfExistingCompany: String
    private lateinit var companyIdOfExistingCompanyWithPermId: String

    @BeforeAll
    fun postDataRequestsBeforeQueryTest() {
        val isinString = generateRandomIsin()
        generateCompaniesWithOneRandomValueForEachIdentifierType(
            mapOf(IdentifierType.isin to isinString),
        )
        val requestA = SingleDataRequest(
            companyIdentifier = isinString,
            dataType = SingleDataRequest.DataType.lksg,
            reportingPeriods = setOf("2022"),
        )
        authenticateAsPremiumUser()
        requestControllerApi.postSingleDataRequest(requestA)

        permIdOfExistingCompany = System.currentTimeMillis().toString()
        generateCompaniesWithOneRandomValueForEachIdentifierType(
            mapOf(IdentifierType.permId to permIdOfExistingCompany),
        )

        val requestB = SingleDataRequest(
            companyIdentifier = permIdOfExistingCompany,
            dataType = SingleDataRequest.DataType.sfdr,
            reportingPeriods = setOf("2021"),
        )
        authenticateAsPremiumUser()
        val req2 = requestControllerApi.postSingleDataRequest(requestB).first()
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        requestControllerApi.patchDataRequestStatus(UUID.fromString(req2.dataRequestId), RequestStatus.answered)
        companyIdOfExistingCompanyWithPermId = getUniqueDatalandCompanyIdForIdentifierValue(permIdOfExistingCompany)
    }

    @Test
    fun `query data requests with no filters and assert that the expected results are being retrieved`() {
        val allDataRequests = requestControllerApi.getDataRequests()
        assertTrue(allDataRequests.isNotEmpty())
    }

    @Test
    fun `query data requests with data type filter and assert that the expected results are being retrieved`() {
        val lksgDataRequests = requestControllerApi.getDataRequests(
            dataType = RequestControllerApi.DataTypeGetDataRequests.lksg,
        )
        assertTrue(lksgDataRequests.isNotEmpty())
        assertTrue(lksgDataRequests.all { it.dataType == StoredDataRequest.DataType.lksg })
    }

    @Test
    fun `query data requests with reporting period filter and assert that the expected results are being retrieved`() {
        val reportingPeriod2021DataRequests = requestControllerApi.getDataRequests(reportingPeriod = "2021")
        assertTrue(reportingPeriod2021DataRequests.isNotEmpty())
        assertTrue(reportingPeriod2021DataRequests.all { it.reportingPeriod == "2021" })
    }

    @Test
    fun `query data requests with request status filter and assert that the expected results are being retrieved`() {
        val resolvedDataRequests = requestControllerApi.getDataRequests(requestStatus = RequestStatus.answered)
        assertTrue(resolvedDataRequests.isNotEmpty())
        assertTrue(resolvedDataRequests.all { it.requestStatus == RequestStatus.answered })
    }

    @Test
    fun `query data requests with company id filter and assert that the expected results are being retrieved`() {
        val specificPermIdDataRequests = requestControllerApi.getDataRequests(
            datalandCompanyId = getUniqueDatalandCompanyIdForIdentifierValue(permIdOfExistingCompany),
        )
        assertTrue(specificPermIdDataRequests.isNotEmpty())
        assertTrue(
            specificPermIdDataRequests.all {
                it.datalandCompanyId == getUniqueDatalandCompanyIdForIdentifierValue(permIdOfExistingCompany)
            },
        )
        assertTrue(specificPermIdDataRequests.all { it.datalandCompanyId == companyIdOfExistingCompanyWithPermId })
    }

    @Test
    fun `query data requests with user id filter and assert that the expected results are being retrieved`() {
        val specificUsersDataRequests = requestControllerApi.getDataRequests(userId = PREMIUM_USER_ID)
        assertTrue(specificUsersDataRequests.isNotEmpty())
        assertTrue(specificUsersDataRequests.all { it.userId == PREMIUM_USER_ID })
    }
}
