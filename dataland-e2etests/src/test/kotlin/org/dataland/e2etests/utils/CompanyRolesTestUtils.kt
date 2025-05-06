package org.dataland.e2etests.utils

import org.dataland.communitymanager.openApiClient.infrastructure.ClientError
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.communitymanager.openApiClient.model.CompanyRoleAssignment
import org.dataland.datalandbackend.openApiClient.model.EutaxonomyNonFinancialsData
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.communityManager.assertErrorCodeInCommunityManagerClientException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class CompanyRolesTestUtils {
    val apiAccessor = ApiAccessor()

    val jwtHelper = JwtAuthenticationHelper()

    private val dataUploaderUserId = UUID.fromString(TechnicalUser.Uploader.technicalUserId)
    private val frameworkSampleData =
        apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getTData(1)[0]

    fun removeBearerTokenFromApiClients() {
        GlobalAuth.setBearerToken(null)
    }

    fun validateCompanyOwnersForCompany(
        companyId: UUID,
        expectedCompanyOwnerUserIds: List<UUID>,
        actualCompanyRoleAssignmentsResponse: List<CompanyRoleAssignment>,
    ) {
        actualCompanyRoleAssignmentsResponse.forEach { companyRoleAssignment ->
            assertEquals(companyId.toString(), companyRoleAssignment.companyId)
            assertEquals(CompanyRole.CompanyOwner, companyRoleAssignment.companyRole)
        }
        val actualCompanyOwnerUserIds = actualCompanyRoleAssignmentsResponse.map { it.userId }

        assertEquals(expectedCompanyOwnerUserIds.size, actualCompanyRoleAssignmentsResponse.size)
        expectedCompanyOwnerUserIds
            .map { it.toString() }
            .forEach { expectedUserId -> assertTrue(actualCompanyOwnerUserIds.contains(expectedUserId)) }
    }

    fun assertAccessDeniedWhenUploadingFrameworkData(
        companyId: UUID,
        dataset: EutaxonomyNonFinancialsData,
        bypassQa: Boolean = false,
    ) {
        val reportingPeriod = "2022"
        val expectedAccessDeniedClientException =
            assertThrows<org.dataland.datalandbackend.openApiClient.infrastructure.ClientException> {
                apiAccessor.euTaxonomyNonFinancialsUploaderFunction(
                    companyId.toString(),
                    dataset,
                    reportingPeriod,
                    bypassQa,
                )
            }
        assertEquals("Client error : 403 ", expectedAccessDeniedClientException.message)
    }

    fun uploadEuTaxoDataWithBypassQa(companyId: UUID) {
        apiAccessor.euTaxonomyNonFinancialsUploaderFunction(companyId.toString(), frameworkSampleData, "2021", true)
    }

    fun assertCompanyNotFoundResponseBodyInCommunityManagerClientException(
        communityManagerClientException: ClientException,
        companyId: UUID,
    ) {
        assertErrorCodeInCommunityManagerClientException(communityManagerClientException, 404)
        val responseBody = (communityManagerClientException.response as ClientError<*>).body as String
        assertTrue(responseBody.contains("Company not found"))
        assertTrue(
            responseBody.contains(
                "\"Dataland does not know the company ID $companyId\"",
            ),
        )
    }

    fun assignCompanyRole(
        companyRole: CompanyRole,
        companyId: UUID,
        userId: UUID,
    ): CompanyRoleAssignment =
        apiAccessor.companyRolesControllerApi
            .assignCompanyRole(companyRole, companyId, userId)

    fun getCompanyRoleAssignments(
        companyRole: CompanyRole? = null,
        companyId: UUID? = null,
        userId: UUID? = null,
    ): List<CompanyRoleAssignment> = apiAccessor.companyRolesControllerApi.getCompanyRoleAssignments(companyRole, companyId, userId)

    fun removeCompanyRole(
        companyRole: CompanyRole,
        companyId: UUID,
        userId: UUID,
    ) {
        apiAccessor.companyRolesControllerApi.removeCompanyRole(companyRole, companyId, userId)
    }

    fun hasUserCompanyRole(
        companyRole: CompanyRole,
        companyId: UUID,
        userId: UUID,
    ) {
        apiAccessor.companyRolesControllerApi.hasUserCompanyRole(companyRole, companyId, userId)
    }

    fun hasCompanyAtLeastOneOwner(companyId: UUID) {
        apiAccessor.companyRolesControllerApi.hasCompanyAtLeastOneOwner(companyId)
    }

    fun uploadCompanyAndReturnCompanyId(): UUID =
        UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )

    fun tryToUseCompanyRoleGetAndHeadEndpointAndAsserThatItsForbidden(companyId: UUID) {
        val exceptionWhenGettingCompanyRolesForAnotherCompany =
            assertThrows<ClientException> {
                getCompanyRoleAssignments(CompanyRole.Member, companyId = companyId)
            }
        assertErrorCodeInCommunityManagerClientException(exceptionWhenGettingCompanyRolesForAnotherCompany, 403)
        val exceptionWhenCheckingCompanyRolesForAnotherCompany =
            assertThrows<ClientException> {
                hasUserCompanyRole(CompanyRole.DataUploader, companyId, dataUploaderUserId)
            }
        assertErrorCodeInCommunityManagerClientException(exceptionWhenCheckingCompanyRolesForAnotherCompany, 403)
    }

    fun tryToAssignAndRemoveCompanyMembersAndAssertThatItsForbidden(companyId: UUID) {
        enumValues<CompanyRole>().forEach {
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            val exceptionWhenTryingToAddCompanyMembers =
                assertThrows<ClientException> {
                    assignCompanyRole(it, companyId, dataUploaderUserId)
                }
            assertErrorCodeInCommunityManagerClientException(exceptionWhenTryingToAddCompanyMembers, 403)

            val exceptionWhenTryingToDeleteCompanyMembers =
                assertThrows<ClientException> {
                    removeCompanyRole(it, companyId, dataUploaderUserId)
                }
            assertErrorCodeInCommunityManagerClientException(exceptionWhenTryingToDeleteCompanyMembers, 403)
        }
    }
}
