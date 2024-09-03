package org.dataland.e2etests.tests.communityManager

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.AccessStatus
import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.communitymanager.openApiClient.model.SingleDataRequest
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.e2etests.auth.GlobalAuth.withTechnicalUser
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.communityManager.generateRandomLei
import org.dataland.e2etests.utils.communityManager.generateRandomPermId
import org.dataland.e2etests.utils.communityManager.getIdForUploadedCompanyWithIdentifiers
import org.dataland.e2etests.utils.communityManager.retrieveTimeAndWaitOneMillisecond
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QueryDataRequestsTest {
    val apiAccessor = ApiAccessor()
    val jwtHelper = JwtAuthenticationHelper()
    private val api = apiAccessor.requestControllerApi

    private val companyIdA = getIdForUploadedCompanyWithIdentifiers(lei = generateRandomLei())
    private val companyIdB = getIdForUploadedCompanyWithIdentifiers(permId = generateRandomPermId())
    private val timestampBeforePost = retrieveTimeAndWaitOneMillisecond()
    private val chunkSize = 1000

    private val sfdrType = RequestControllerApi.DataTypeGetDataRequests.sfdr
    private val p2pType = RequestControllerApi.DataTypeGetDataRequests.p2p
    private val vsmeType = RequestControllerApi.DataTypeGetDataRequests.vsme

    private fun postSingleDataRequest(
        companyId: String,
        dataType: SingleDataRequest.DataType,
        reportingPeriods: Set<String>,
    ) {
        api.postSingleDataRequest(
            SingleDataRequest(
                companyIdentifier = companyId,
                dataType = dataType,
                reportingPeriods = reportingPeriods,
            ),
        )
    }

    private fun assignCompanyOwnershipToUser(companyId: String, userId: String) {
        withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.companyRolesControllerApi.assignCompanyRole(
                CompanyRole.CompanyOwner,
                UUID.fromString(companyId),
                UUID.fromString(userId),
            )
        }
    }

    private fun removeCompanyOwnershipFromUser(companyId: String, userId: String) {
        withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.companyRolesControllerApi.removeCompanyRole(
                CompanyRole.CompanyOwner,
                UUID.fromString(companyId),
                UUID.fromString(userId),
            )
        }
    }

    private fun assertAccessDeniedWrapper(
        operation: () -> Any,
    ) {
        val expectedAccessDeniedClientException = assertThrows<ClientException> {
            operation()
        }
        assertEquals("Client error : 403 ", expectedAccessDeniedClientException.message)
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
    fun `query data requests with no filters and assert that the expected results are being retrieved`() {
        val storedDataRequests = api.getDataRequests(chunkSize = chunkSize)
            .filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(3, storedDataRequests.size)
    }

    @Test
    fun `query data requests with data type filter and assert that the expected results are being retrieved`() {
        val sfdrDataRequests = api.getDataRequests(dataType = listOf(sfdrType), chunkSize = chunkSize)
            .filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(0, sfdrDataRequests.size)

        val p2pDataRequests = api.getDataRequests(dataType = listOf(p2pType), chunkSize = chunkSize)
            .filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(1, p2pDataRequests.size)
        assertEquals(DataTypeEnum.p2p.value, p2pDataRequests.first().dataType)

        val vsmeDataRequests = api.getDataRequests(dataType = listOf(vsmeType), chunkSize = chunkSize)
            .filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(2, vsmeDataRequests.size)
        vsmeDataRequests.forEach { assertEquals(DataTypeEnum.vsme.value, it.dataType) }

        val vsmeAndP2pDataRequests = api.getDataRequests(dataType = listOf(vsmeType, p2pType), chunkSize = chunkSize)
            .filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(3, vsmeAndP2pDataRequests.size)
        vsmeDataRequests.forEach {
            assertTrue(listOf(DataTypeEnum.vsme.value, DataTypeEnum.p2p.value).contains(it.dataType))
        }
    }

    @Test
    fun `query data requests with reporting period filter and assert that the expected results are being retrieved`() {
        val dataRequestsFor2021 = api.getDataRequests(reportingPeriod = "2021", chunkSize = chunkSize)
            .filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(0, dataRequestsFor2021.size)

        val dataRequestsFor2022 = api.getDataRequests(reportingPeriod = "2022", chunkSize = chunkSize)
            .filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(1, dataRequestsFor2022.size)
        assertEquals("2022", dataRequestsFor2022.first().reportingPeriod)

        val dataRequestsFor2023 = api.getDataRequests(reportingPeriod = "2023", chunkSize = chunkSize)
            .filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(2, dataRequestsFor2023.size)
        dataRequestsFor2023.forEach { assertEquals("2023", it.reportingPeriod) }
    }

    @Test
    fun `query data requests with request status filter and assert that the expected results are being retrieved`() {
        val resolvedDataRequests =
            api.getDataRequests(requestStatus = setOf(RequestStatus.Resolved), chunkSize = chunkSize)
                .filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(0, resolvedDataRequests.size)

        val dataRequestIdB = UUID.fromString(
            api.getDataRequests(chunkSize = chunkSize).filter { it.creationTimestamp > timestampBeforePost }
                .first { it.datalandCompanyId == companyIdB }.dataRequestId,
        )
        val storedDataRequestB = api.getDataRequestById(dataRequestIdB)
        assertEquals(DataTypeEnum.p2p.value, storedDataRequestB.dataType)
        assertEquals("2023", storedDataRequestB.reportingPeriod)

        api.patchDataRequest(dataRequestIdB, RequestStatus.Answered)

        val answeredDataRequests =
            api.getDataRequests(requestStatus = setOf(RequestStatus.Answered), chunkSize = chunkSize)
                .filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(1, answeredDataRequests.size)
        assertEquals(companyIdB, answeredDataRequests.first().datalandCompanyId)
        assertEquals(RequestStatus.Answered, answeredDataRequests.first().requestStatus)

        val answeredAndOpenRequests = api.getDataRequests(
            requestStatus = setOf(RequestStatus.Answered, RequestStatus.Open),
            chunkSize = chunkSize,
        )
            .filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(3, answeredAndOpenRequests.size)
    }

    @Test
    fun `query data requests with access status filter and assert that the expected results are being retrieved`() {
        val grantedAccessRequests =
            api.getDataRequests(accessStatus = setOf(AccessStatus.Granted), chunkSize = chunkSize)
                .filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(0, grantedAccessRequests.size)

        val publicAccessRequests =
            api.getDataRequests(accessStatus = setOf(AccessStatus.Public), chunkSize = chunkSize)
                .filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(1, publicAccessRequests.size)
        assertEquals(companyIdB, publicAccessRequests.first().datalandCompanyId)
        assertEquals(AccessStatus.Public, publicAccessRequests.first().accessStatus)

        val pendingAndPublicAccessRequests = api.getDataRequests(
            accessStatus = setOf(AccessStatus.Pending, AccessStatus.Public),
            chunkSize = chunkSize,
        ).filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(3, pendingAndPublicAccessRequests.size)
    }

    @Test
    fun `query data requests with company id filter and assert that the expected results are being retrieved`() {
        val storedDataRequestsForRandomCompanyId =
            api.getDataRequests(datalandCompanyId = UUID.randomUUID().toString(), chunkSize = chunkSize)
                .filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(0, storedDataRequestsForRandomCompanyId.size)

        val storedDataRequestsForCompanyB =
            api.getDataRequests(datalandCompanyId = companyIdB, chunkSize = chunkSize)
                .filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(1, storedDataRequestsForCompanyB.size)
        assertEquals(companyIdB, storedDataRequestsForCompanyB.first().datalandCompanyId)

        val storedDataRequestsForCompanyA =
            api.getDataRequests(datalandCompanyId = companyIdA, chunkSize = chunkSize)
                .filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(2, storedDataRequestsForCompanyA.size)
        storedDataRequestsForCompanyA.forEach { assertEquals(companyIdA, it.datalandCompanyId) }
    }

    @Test
    fun `query data requests with user id filter and assert that the expected results are being retrieved`() {
        val dataRequestsByAdmin =
            api.getDataRequests(userId = TechnicalUser.Admin.technicalUserId, chunkSize = chunkSize)
                .filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(0, dataRequestsByAdmin.size)

        val dataRequestsByPremiumUser =
            api.getDataRequests(userId = TechnicalUser.PremiumUser.technicalUserId, chunkSize = chunkSize)
                .filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(3, dataRequestsByPremiumUser.size)
        dataRequestsByPremiumUser.forEach { assertEquals(TechnicalUser.PremiumUser.technicalUserId, it.userId) }
    }

    @Test
    fun `query data requests and assert that email address is only populated for admin or owned companies`() {
        withTechnicalUser(TechnicalUser.Admin) {
            val dataRequests = api.getDataRequests(datalandCompanyId = companyIdA)
            assertTrue(dataRequests.all { it.userEmailAddress != null })
        }

        val queryingUser = TechnicalUser.Reader

        withTechnicalUser(queryingUser) {
            assertAccessDeniedWrapper { api.getDataRequests(datalandCompanyId = companyIdA) }
        }

        assignCompanyOwnershipToUser(companyIdA, queryingUser.technicalUserId)

        withTechnicalUser(queryingUser) {
            val dataRequests = api.getDataRequests(datalandCompanyId = companyIdA)
            assertEquals(2, dataRequests.size)
            assertTrue(dataRequests.all { it.userEmailAddress != null })
        }

        removeCompanyOwnershipFromUser(companyIdA, queryingUser.technicalUserId)

        withTechnicalUser(queryingUser) {
            assertAccessDeniedWrapper { api.getDataRequests(datalandCompanyId = companyIdA) }
        }
    }

    @Test
    fun `query data requests and assert that email address is not visible for public data requests`() {
        val queryingUser = TechnicalUser.Reader

        withTechnicalUser(queryingUser) {
            assertAccessDeniedWrapper { api.getDataRequests(datalandCompanyId = companyIdB) }
        }

        assignCompanyOwnershipToUser(companyIdB, queryingUser.technicalUserId)

        withTechnicalUser(queryingUser) {
            val dataRequests = api.getDataRequests(datalandCompanyId = companyIdB)
            assertEquals(1, dataRequests.size)
            assertTrue(dataRequests.all { it.userEmailAddress == null })
        }

        removeCompanyOwnershipFromUser(companyIdB, queryingUser.technicalUserId)

        withTechnicalUser(queryingUser) {
            assertAccessDeniedWrapper { api.getDataRequests(datalandCompanyId = companyIdB) }
        }
    }

    /*TODO Emanuel: Wir können hier den email-Addressen-Filter nicht testen, da die technischen User keine E-Mail-
    Adresse haben => Wir müssen das unbedingt über unit-Tests o.Ä. covern!
    Falls das nicht funktioniert oder keinen Sinn macht => e2e-Test mit einem fake-User mit non-blank email-Adresse
    */

    @Test
    fun `query data requests with combined filter and assert that the expected results are being retrieved`() {
        val combinedQueryResults =
            api.getDataRequests(
                dataType = listOf(sfdrType, p2pType, vsmeType),
                requestStatus = setOf(RequestStatus.Open, RequestStatus.Resolved),
                accessStatus = setOf(AccessStatus.Pending),
                reportingPeriod = "2023",
                chunkSize = chunkSize,
            )
                .filter { it.creationTimestamp > timestampBeforePost }
        assertEquals(1, combinedQueryResults.size)
    }
}
