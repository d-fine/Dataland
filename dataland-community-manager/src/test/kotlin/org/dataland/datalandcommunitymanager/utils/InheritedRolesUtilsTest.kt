package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandbackendutils.model.InheritedRole
import org.dataland.datalandcommunitymanager.model.companyRights.CompanyRight
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InheritedRolesUtilsTest {
    @Suppress("UnusedPrivateMember") // detekt wrongly thinks this function is unused
    private fun companyRightsListSource(): List<Arguments> =
        listOf(
            Arguments.of(emptyList<CompanyRight>()),
            Arguments.of(listOf(CompanyRight.Member)),
            Arguments.of(listOf(CompanyRight.Provider)),
            Arguments.of(listOf(CompanyRight.Member, CompanyRight.Provider)),
        )

    @ParameterizedTest
    @MethodSource("companyRightsListSource")
    fun `check that company rights are translated to inherited roles as expected`(companyRightsList: List<CompanyRight>) {
        val expectedInheritedRolesList =
            when (companyRightsList) {
                emptyList<CompanyRight>() -> emptyList<InheritedRole>()
                listOf(CompanyRight.Member) -> listOf(InheritedRole.DatalandMember)
                listOf(CompanyRight.Provider) -> emptyList<InheritedRole>()
                listOf(CompanyRight.Member, CompanyRight.Provider) -> listOf(InheritedRole.DatalandMember)
                else -> throw IllegalArgumentException("Unexpected company rights list: $companyRightsList")
            }

        val actualInheritedRolesList = InheritedRolesUtils.getInheritedRoles(companyRightsList)

        assertEquals(expectedInheritedRolesList, actualInheritedRolesList)
    }
}
