package org.dataland.e2etests.tests

import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.communitymanager.openApiClient.model.KeycloakUserInfo
import org.dataland.datalandbackend.openApiClient.model.CompanyInformationPatch
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.CompanyRolesTestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EmailAddressControllerTest {
    private val apiAccessor = ApiAccessor()
    private val jwtHelper = JwtAuthenticationHelper()
    private val companyRolesTestUtils = CompanyRolesTestUtils()

    private fun assignCompanyRole(
        user: TechnicalUser,
        companyId: UUID,
        role: CompanyRole,
    ) {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        companyRolesTestUtils.assignCompanyRole(
            role,
            companyId,
            UUID.fromString(user.technicalUserId),
        )
    }

    private fun assignCompanyRoleAndEnsureEmailSubdomainEndpointDoesNotThrow(
        user: TechnicalUser,
        companyId: UUID,
        role: CompanyRole,
    ): List<KeycloakUserInfo> {
        assignCompanyRole(user, companyId, role)
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(user)

        return assertDoesNotThrow {
            apiAccessor.emailAddressControllerApi.getUsersByCompanyAssociatedSubdomains(companyId)
        }
    }

    private fun assignCompanyRoleAndEnsureEmailSubdomainEndpointThrows(
        user: TechnicalUser,
        companyId: UUID,
        role: CompanyRole,
    ) {
        assignCompanyRole(user, companyId, role)
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(user)

        assertThrows<ClientException> {
            apiAccessor.emailAddressControllerApi.getUsersByCompanyAssociatedSubdomains(companyId)
        }
    }

    @ParameterizedTest
    @EnumSource(value = TechnicalUser::class)
    fun `ensure that only Dataland admins or company owners or member admins can query users by email subdomain`(user: TechnicalUser) {
        val companyId = companyRolesTestUtils.uploadCompanyAndReturnCompanyId()
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(user)

        when (user) {
            TechnicalUser.Admin ->
                assertDoesNotThrow {
                    apiAccessor.emailAddressControllerApi.getUsersByCompanyAssociatedSubdomains(companyId)
                }

            else ->
                assertThrows<ClientException> {
                    apiAccessor.emailAddressControllerApi.getUsersByCompanyAssociatedSubdomains(companyId)
                }
        }

        if (user == TechnicalUser.Admin) return

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        apiAccessor.companyDataControllerApi.patchCompanyById(
            companyId = companyId.toString(),
            companyInformationPatch =
                CompanyInformationPatch(
                    associatedSubdomains = listOf("example"),
                ),
        )

        CompanyRole.entries.forEach { role ->
            when (role) {
                CompanyRole.CompanyOwner, CompanyRole.MemberAdmin -> {
                    val keycloakUserInfos =
                        assignCompanyRoleAndEnsureEmailSubdomainEndpointDoesNotThrow(
                            user, companyId, role,
                        )
                    assertEquals(TechnicalUser.entries.size, keycloakUserInfos.size)
                    TechnicalUser.entries.forEach { technicalUser ->
                        assert(
                            keycloakUserInfos.any { userInfo ->
                                userInfo.id == technicalUser.technicalUserId.toString()
                            },
                        )
                    }
                }

                else -> {
                    assignCompanyRoleAndEnsureEmailSubdomainEndpointThrows(
                        user, companyId, role,
                    )
                }
            }
        }
    }
}
