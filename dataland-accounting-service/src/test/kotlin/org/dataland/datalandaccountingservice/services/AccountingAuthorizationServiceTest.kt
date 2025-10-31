package org.dataland.datalandaccountingservice.services

import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.model.CompanyRoleAssignmentExtended
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountingAuthorizationServiceTest {
    private val mockCompanyRolesControllerApi = mock<CompanyRolesControllerApi>()

    private lateinit var accountingAuthorizationService: AccountingAuthorizationService

    private val companyId = UUID.randomUUID()
    private val userId = UUID.randomUUID()

    private val mockSecurityContext = mock<SecurityContext>()

    private val mockAuthentication =
        AuthenticationMock.mockJwtAuthentication(
            username = "JohnDoe",
            userId = userId.toString(),
            roles = setOf(DatalandRealmRole.ROLE_USER),
        )

    @BeforeEach
    fun setup() {
        reset(mockCompanyRolesControllerApi, mockSecurityContext)

        doReturn(mockAuthentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)

        accountingAuthorizationService = AccountingAuthorizationService(companyRolesControllerApi = mockCompanyRolesControllerApi)
    }

    @Test
    fun `hasUserSomeRoleInCompany returns false when there are no matching role assignments`() {
        doReturn(emptyList<CompanyRoleAssignmentExtended>()).whenever(mockCompanyRolesControllerApi).getExtendedCompanyRoleAssignments(
            role = null,
            companyId = companyId,
            userId = userId,
        )

        val result = accountingAuthorizationService.hasUserSomeRoleInCompany(companyId.toString())

        assert(!result)
    }

    @Test
    fun `hasUserSomeRoleInCompany returns true when there is at least one matching role`() {
        doReturn(
            listOf(mock<CompanyRoleAssignmentExtended>()),
        ).whenever(mockCompanyRolesControllerApi).getExtendedCompanyRoleAssignments(
            role = null,
            companyId = companyId,
            userId = userId,
        )

        val result = accountingAuthorizationService.hasUserSomeRoleInCompany(companyId.toString())

        assert(result)
    }
}
