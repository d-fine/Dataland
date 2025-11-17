package org.dataland.datalandaccountingservice.services

import org.dataland.datalandbackendutils.model.InheritedRole
import org.dataland.datalandcommunitymanager.openApiClient.api.InheritedRolesControllerApi
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
    private val mockIngeritedRolesControllerApi = mock<InheritedRolesControllerApi>()

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
        reset(mockIngeritedRolesControllerApi, mockSecurityContext)

        doReturn(mockAuthentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)

        accountingAuthorizationService = AccountingAuthorizationService(inheritedRolesControllerApi = mockIngeritedRolesControllerApi)
    }

    @Test
    fun `hasUserSomeRoleInCompany returns false when there are no matching role assignments`() {
        doReturn(emptyMap<String, List<String>>()).whenever(mockIngeritedRolesControllerApi).getInheritedRoles(
            userId.toString(),
        )

        val result = accountingAuthorizationService.hasUserRoleInMemberCompany(companyId.toString())

        assert(!result)
    }

    @Test
    fun `hasUserSomeRoleInCompany returns true when there is at least one matching role`() {
        doReturn(
            mapOf(
                companyId.toString() to listOf(InheritedRole.DatalandMember.name),
            ),
        ).whenever(mockIngeritedRolesControllerApi).getInheritedRoles(
            userId.toString(),
        )

        val result = accountingAuthorizationService.hasUserRoleInMemberCompany(companyId.toString())

        assert(result)
    }
}
