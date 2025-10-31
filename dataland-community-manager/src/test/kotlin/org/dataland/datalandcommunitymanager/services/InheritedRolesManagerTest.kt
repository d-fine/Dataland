package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackendutils.model.InheritedRole
import org.dataland.datalandcommunitymanager.entities.CompanyRoleAssignmentEntity
import org.dataland.datalandcommunitymanager.model.companyRights.CompanyRight
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InheritedRolesManagerTest {
    private val mockCompanyRolesManager = mock<CompanyRolesManager>()
    private val mockCompanyRightsManager = mock<CompanyRightsManager>()
    private lateinit var inheritedRolesManager: InheritedRolesManager

    private val userId = UUID.randomUUID()

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
            userId = userId.toString(),
        )

    @BeforeEach
    fun setup() {
        reset(
            mockCompanyRolesManager,
            mockCompanyRightsManager,
        )

        doReturn(
            companyIds.map { createCompanyRoleAssignmentEntity(it) },
        ).whenever(mockCompanyRolesManager).getCompanyRoleAssignmentsByParameters(
            companyRole = null,
            companyId = null,
            userId = userId.toString(),
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
        val actualInheritedRolesMap = inheritedRolesManager.getInheritedRoles(userId)
        assertEquals(expectedInheritedRolesMap, actualInheritedRolesMap)
    }
}
