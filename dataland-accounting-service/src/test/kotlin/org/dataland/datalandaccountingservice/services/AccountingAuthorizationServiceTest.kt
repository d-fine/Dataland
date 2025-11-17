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
import kotlin.collections.listOf

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
    fun `hasUserRoleInMemberCompany returns false when the user does not belong tio any company`() {
        doReturn(emptyMap<String, List<String>>()).whenever(mockIngeritedRolesControllerApi).getInheritedRoles(
            userId.toString(),
        )

        val result = accountingAuthorizationService.hasUserRoleInMemberCompany(companyId.toString())

        assert(!result)
    }

    @Test
    fun `hasUserRoleInMemberCompany returns false when the company is not a member`() {
        doReturn(
            mapOf(
                companyId.toString() to emptyList<String>(),
            ),
        ).whenever(mockIngeritedRolesControllerApi).getInheritedRoles(
            userId.toString(),
        )

        val result = accountingAuthorizationService.hasUserRoleInMemberCompany(companyId.toString())

        assert(!result)
    }

    @Test
    fun `hasUserRoleInMemberCompany returns false when the user belongs to a different member company`() {
        doReturn(
            mapOf(
                UUID.randomUUID() to listOf(InheritedRole.DatalandMember.name),
            ),
        ).whenever(mockIngeritedRolesControllerApi).getInheritedRoles(
            userId.toString(),
        )

        val result = accountingAuthorizationService.hasUserRoleInMemberCompany(companyId.toString())

        assert(!result)
    }

    @Test
    fun `hasUserRoleInMemberCompany returns true when the user belongs to a member company`() {
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
