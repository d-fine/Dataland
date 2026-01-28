package org.dataland.batchmanager.service
import org.dataland.dataSourcingService.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandbatchmanager.service.DerivedRightsUtilsComponent
import org.dataland.datalandbatchmanager.service.PortfolioSharingUpdater
import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.model.CompanyRole
import org.dataland.datalandcommunitymanager.openApiClient.model.CompanyRoleAssignmentExtended
import org.dataland.userService.openApiClient.api.PortfolioControllerApi
import org.dataland.userService.openApiClient.model.BasePortfolio
import org.dataland.userService.openApiClient.model.NotificationFrequency
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class PortfolioSharingUpdaterTest {
    private val mockPortfolioControllerApi = mock<PortfolioControllerApi>()
    private val mockCompanyRolesControllerApi = mock<CompanyRolesControllerApi>()
    private val mockKeycloakUserService = mock<KeycloakUserService>()
    private val mockDerivedRightsUtilsComponent = mock<DerivedRightsUtilsComponent>()

    private lateinit var portfolioSharingUpdater: PortfolioSharingUpdater

    @BeforeEach
    fun setUp() {
        portfolioSharingUpdater =
            PortfolioSharingUpdater(
                companyRolesControllerApi = mockCompanyRolesControllerApi,
                portfolioControllerApi = mockPortfolioControllerApi,
                keycloakUserService = mockKeycloakUserService,
                resultsPerPage = 100,
                derivedRightsUtilsComponent = mockDerivedRightsUtilsComponent,
            )
    }

    @Test
    fun `getAllUserIdsOfAdminsAndMembers combines admins and members`() {
        val adminUserOne = createKeycloakUser("admin-id")
        val adminUserTwo = createKeycloakUser("admin-id-2")
        whenever(mockKeycloakUserService.getUsersByRole("ROLE_ADMIN")).thenReturn(listOf(adminUserOne, adminUserTwo))

        val memberAssignment = createCompanyRoleAssignment("member-id")
        val nonMemberAssignment = createCompanyRoleAssignment("non-member-id")

        whenever(mockCompanyRolesControllerApi.getExtendedCompanyRoleAssignments())
            .thenReturn(listOf(memberAssignment, nonMemberAssignment))

        whenever(mockDerivedRightsUtilsComponent.isUserDatalandMember(memberAssignment.userId)).thenReturn(true)
        whenever(mockDerivedRightsUtilsComponent.isUserDatalandMember(nonMemberAssignment.userId)).thenReturn(false)

        val result = portfolioSharingUpdater.getAllUserIdsOfAdminsAndMembers()

        assert(result.contains(adminUserOne.userId))
        assert(result.contains(adminUserTwo.userId))
        assert(result.contains(memberAssignment.userId))
        assert(!result.contains(nonMemberAssignment.userId))
    }

    @Test
    fun `getAllUserIdsOfAdminsAndMembers throws an exception if no admins or members are found`() {
        whenever(mockKeycloakUserService.getUsersByRole("ROLE_ADMIN")).thenReturn(emptyList())
        whenever(mockCompanyRolesControllerApi.getExtendedCompanyRoleAssignments()).thenReturn(emptyList())

        val exception =
            assertThrows<IllegalArgumentException> {
                portfolioSharingUpdater.getAllUserIdsOfAdminsAndMembers()
            }
        assert(exception.message == "No Dataland admins or members found. Portfolio sharing update failed.")
    }

    @Test
    fun `getAllUserIdsOfAdminsAndMembers only returns single userId if user is admin and member`() {
        whenever(mockKeycloakUserService.getUsersByRole("ROLE_ADMIN")).thenReturn(listOf(createKeycloakUser("user-id")))
        whenever(mockCompanyRolesControllerApi.getExtendedCompanyRoleAssignments())
            .thenReturn(listOf(createCompanyRoleAssignment("user-id")))
        whenever(mockDerivedRightsUtilsComponent.isUserDatalandMember("user-id")).thenReturn(true)

        val result = portfolioSharingUpdater.getAllUserIdsOfAdminsAndMembers()
        assert(result.size == 1)
    }

    @Test
    fun `updatePortfolioSharing removes sharing when user is no longer member or admin`() {
        val spyUpdater = spy(portfolioSharingUpdater)

        doReturn(setOf("u1")).whenever(spyUpdater).getAllUserIdsOfAdminsAndMembers()

        val p1 = createBasePortfolio(portfolioId = "p1", userId = "u1", sharedUserIds = setOf("u2"))
        val p2 = createBasePortfolio(portfolioId = "p2", userId = "u2", sharedUserIds = setOf("u1"))
        val p3 = createBasePortfolio(portfolioId = "p3", userId = "u2", sharedUserIds = emptySet())

        whenever(mockPortfolioControllerApi.getAllPortfolios(chunkSize = 100, chunkIndex = 0)).thenReturn(listOf(p1, p2, p3))
        whenever(mockPortfolioControllerApi.getAllPortfolios(chunkSize = 100, chunkIndex = 1)).thenReturn(emptyList())

        spyUpdater.updatePortfolioSharing()

        verify(mockPortfolioControllerApi, times(0)).patchSharing(eq("p1"), any())
        verify(mockPortfolioControllerApi, times(1)).patchSharing(eq("p2"), argThat { sharedUserIds.isEmpty() })
        verify(mockPortfolioControllerApi, times(0)).patchSharing(eq("p3"), any())
    }

    @Test
    fun `updatePortfolioSharing breaks when chunkOfPortfolios is empty`() {
        val spyUpdater = spy(portfolioSharingUpdater)
        doReturn(setOf("u1")).whenever(spyUpdater).getAllUserIdsOfAdminsAndMembers()

        whenever(mockPortfolioControllerApi.getAllPortfolios(chunkSize = 100, chunkIndex = 0)).thenReturn(emptyList())

        spyUpdater.updatePortfolioSharing()

        verify(mockPortfolioControllerApi, times(1)).getAllPortfolios(chunkSize = 100, chunkIndex = 0)
        verify(mockPortfolioControllerApi, times(0)).patchSharing(eq("p1"), any())
    }

    @Test
    fun `updatePortfolioSharing breaks when chunkOfPortfolios is smaller than resultsPerPage`() {
        val spyUpdater = spy(portfolioSharingUpdater)
        doReturn(setOf("u1")).whenever(spyUpdater).getAllUserIdsOfAdminsAndMembers()

        val p1 = createBasePortfolio(portfolioId = "p1", userId = "u1", sharedUserIds = setOf("x"))

        whenever(mockPortfolioControllerApi.getAllPortfolios(chunkSize = 100, chunkIndex = 0)).thenReturn(listOf(p1))

        spyUpdater.updatePortfolioSharing()

        verify(mockPortfolioControllerApi, times(1)).getAllPortfolios(chunkSize = 100, chunkIndex = 0)
        verify(mockPortfolioControllerApi, times(0)).patchSharing(eq("p1"), any())
    }

    @Test
    fun `updatePortfolioSharing logs error but continues if patchSharing fails`() {
        val spyUpdater = spy(portfolioSharingUpdater)

        doReturn(setOf("u1")).whenever(spyUpdater).getAllUserIdsOfAdminsAndMembers()

        val p1 = createBasePortfolio(portfolioId = "p1", userId = "u2", sharedUserIds = setOf("x"))
        val p2 = createBasePortfolio(portfolioId = "p2", userId = "u3", sharedUserIds = setOf("y"))

        whenever(mockPortfolioControllerApi.getAllPortfolios(eq(100), any()))
            .thenReturn(listOf(p1, p2))
            .thenReturn(emptyList())

        whenever(mockPortfolioControllerApi.patchSharing(eq("p1"), any()))
            .thenThrow(ClientException(statusCode = 400, message = "Bad Request"))

        whenever(mockPortfolioControllerApi.patchSharing(eq("p2"), any()))
            .thenReturn(mock())

        spyUpdater.updatePortfolioSharing()

        verify(mockPortfolioControllerApi, times(1)).patchSharing(eq("p1"), any())
        verify(mockPortfolioControllerApi, times(1)).patchSharing(eq("p2"), any())

        verify(mockPortfolioControllerApi, times(1)).getAllPortfolios(eq(100), eq(0))
        verify(mockPortfolioControllerApi, never()).getAllPortfolios(eq(100), eq(1))
    }

    @Test
    fun `updatePortfolioSharing handles ClientException gracefully`() {
        val spyUpdater = spy(portfolioSharingUpdater)
        doReturn(setOf("admin")).whenever(spyUpdater).getAllUserIdsOfAdminsAndMembers()

        val p1 = createBasePortfolio(portfolioId = "fail-400", userId = "stranger", sharedUserIds = setOf("u1"))

        whenever(mockPortfolioControllerApi.getAllPortfolios(eq(100), any()))
            .thenReturn(listOf(p1))
            .thenReturn(emptyList())

        whenever(mockPortfolioControllerApi.patchSharing(eq("fail-400"), any()))
            .thenThrow(
                ClientException(
                    statusCode = 400,
                    message = "Invalid Portfolio ID",
                ),
            )

        spyUpdater.updatePortfolioSharing()
        verify(mockPortfolioControllerApi).patchSharing(eq("fail-400"), any())
    }

    @Test
    fun `updatePortfolioSharing handles ServerException gracefully`() {
        val spyUpdater = spy(portfolioSharingUpdater)
        doReturn(setOf("admin")).whenever(spyUpdater).getAllUserIdsOfAdminsAndMembers()

        val p1 = createBasePortfolio(portfolioId = "fail-500", userId = "stranger", sharedUserIds = setOf("u1"))
        val p2 = createBasePortfolio(portfolioId = "success", userId = "stranger", sharedUserIds = setOf("u1"))

        whenever(mockPortfolioControllerApi.getAllPortfolios(eq(100), any()))
            .thenReturn(listOf(p1, p2))
            .thenReturn(emptyList())

        whenever(mockPortfolioControllerApi.patchSharing(eq("fail-500"), any()))
            .thenThrow(
                org.dataland.userService.openApiClient.infrastructure.ServerException(
                    statusCode = 500,
                    message = "Internal Server Error",
                ),
            )

        spyUpdater.updatePortfolioSharing()
        verify(mockPortfolioControllerApi).patchSharing(eq("success"), any())
    }

    @Test
    fun `updatePortfolioSharing handles IllegalStateException`() {
        val spyUpdater = spy(portfolioSharingUpdater)
        doReturn(setOf("admin")).whenever(spyUpdater).getAllUserIdsOfAdminsAndMembers()

        val p1 = createBasePortfolio(portfolioId = "misconfigured", userId = "stranger", sharedUserIds = setOf("u1"))

        whenever(mockPortfolioControllerApi.getAllPortfolios(eq(100), any()))
            .thenReturn(listOf(p1))
            .thenReturn(emptyList())

        whenever(mockPortfolioControllerApi.patchSharing(eq("misconfigured"), any()))
            .thenThrow(IllegalStateException("Moshi mapping failed"))

        spyUpdater.updatePortfolioSharing()

        verify(mockPortfolioControllerApi).patchSharing(eq("misconfigured"), any())
    }

    fun createBasePortfolio(
        portfolioId: String,
        userId: String,
        sharedUserIds: Set<String>,
    ) = BasePortfolio(
        portfolioId = portfolioId,
        userId = userId,
        portfolioName = "Test Portfolio",
        creationTimestamp = 1234L,
        lastUpdateTimestamp = 5678L,
        identifiers = emptySet(),
        isMonitored = false,
        monitoredFrameworks = emptySet(),
        notificationFrequency = NotificationFrequency.Weekly,
        sharedUserIds = sharedUserIds,
    )

    /**
     * Creates a CompanyRoleAssignmentExtended object with a random companyId and userId.
     */
    fun createCompanyRoleAssignment(userId: String): CompanyRoleAssignmentExtended =
        CompanyRoleAssignmentExtended(
            userId = userId,
            companyRole = CompanyRole.Analyst,
            companyId = UUID.randomUUID().toString(),
            email = "test@data.test.com",
            firstName = "First",
            lastName = "Last",
        )

    /**
     * Creates a KeycloakUserInfo object with a random userId.
     */
    fun createKeycloakUser(id: String) =
        KeycloakUserInfo(
            email = "test@data.test.com",
            userId = id,
            firstName = "Test",
            lastName = "User",
        )
}
