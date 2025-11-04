package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackendutils.model.InheritedRole
import org.dataland.datalandcommunitymanager.entities.CompanyRoleAssignmentEntity
import org.dataland.datalandcommunitymanager.model.companyRights.CompanyRight
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
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
class InheritedRolesManagerTest {
    private val mockCompanyRolesManager = mock<CompanyRolesManager>()
    private val mockCompanyRightsManager = mock<CompanyRightsManager>()
    private lateinit var inheritedRolesManager: InheritedRolesManager

    private val adminId = UUID.randomUUID()
    private val nonAdminId = UUID.randomUUID()

    private val dummyAdminAuthentication =
        AuthenticationMock.mockJwtAuthentication(
            username = "DATA_ADMIN",
            userId = adminId.toString(),
            roles = setOf(DatalandRealmRole.ROLE_ADMIN),
        )

    private val dummyNonAdminAuthentication =
        AuthenticationMock.mockJwtAuthentication(
            username = "DATA_READER",
            userId = nonAdminId.toString(),
            roles = setOf(DatalandRealmRole.ROLE_USER),
        )

    private val mockSecurityContext = mock<SecurityContext>()

    private val companyIdWithoutRights = UUID.randomUUID()
    private val companyIdWithMemberOnlyRights = UUID.randomUUID()
    private val companyIdWithProviderOnlyRights = UUID.randomUUID()
    private val companyIdWithBothRights = UUID.randomUUID()

    private val companyIds =
        listOf(
            companyIdWithoutRights,
            companyIdWithMemberOnlyRights,
            companyIdWithProviderOnlyRights,
            companyIdWithBothRights,
        )

    private val expectedInheritedRolesMap =
        mapOf(
            companyIdWithoutRights.toString() to emptyList<String>(),
            companyIdWithMemberOnlyRights.toString() to listOf(InheritedRole.DatalandMember.name),
            companyIdWithProviderOnlyRights.toString() to emptyList<String>(),
            companyIdWithBothRights.toString() to listOf(InheritedRole.DatalandMember.name),
        )

    private fun createCompanyRoleAssignmentEntity(companyId: UUID): CompanyRoleAssignmentEntity =
        CompanyRoleAssignmentEntity(
            companyRole = CompanyRole.entries.random(),
            companyId = companyId.toString(),
            userId = nonAdminId.toString(),
        )

    @BeforeEach
    fun setup() {
        reset(
            mockCompanyRolesManager,
            mockCompanyRightsManager,
            mockSecurityContext,
        )

        doReturn(
            companyIds.map { createCompanyRoleAssignmentEntity(it) },
        ).whenever(mockCompanyRolesManager).getCompanyRoleAssignmentsByParameters(
            companyRole = null,
            companyId = null,
            userId = nonAdminId.toString(),
        )
        doReturn(emptyList<CompanyRight>()).whenever(mockCompanyRightsManager).getCompanyRights(companyIdWithoutRights)
        doReturn(listOf(CompanyRight.Member)).whenever(mockCompanyRightsManager).getCompanyRights(companyIdWithMemberOnlyRights)
        doReturn(listOf(CompanyRight.Provider)).whenever(mockCompanyRightsManager).getCompanyRights(companyIdWithProviderOnlyRights)
        doReturn(
            listOf(CompanyRight.Member, CompanyRight.Provider),
        ).whenever(mockCompanyRightsManager).getCompanyRights(companyIdWithBothRights)

        inheritedRolesManager =
            InheritedRolesManager(
                companyRolesManager = mockCompanyRolesManager,
                companyRightsManager = mockCompanyRightsManager,
            )
    }

    @Test
    fun `check that getInheritedRoles behaves as expected`() {
        val actualInheritedRolesMap = inheritedRolesManager.getInheritedRoles(nonAdminId)
        assertEquals(expectedInheritedRolesMap, actualInheritedRolesMap)
    }

    @Test
    fun `check that requesterMayQueryInheritedRoles behaves as expected for admins`() {
        doReturn(dummyAdminAuthentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)

        assert(inheritedRolesManager.requesterMayQueryInheritedRoles(adminId.toString()))
        assert(inheritedRolesManager.requesterMayQueryInheritedRoles(nonAdminId.toString()))
    }

    @Test
    fun `check that requesterMayQueryInheritedRoles behaves as expected for nonadmins`() {
        doReturn(dummyNonAdminAuthentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)

        assert(!inheritedRolesManager.requesterMayQueryInheritedRoles(adminId.toString()))
        assert(inheritedRolesManager.requesterMayQueryInheritedRoles(nonAdminId.toString()))
    }
}
