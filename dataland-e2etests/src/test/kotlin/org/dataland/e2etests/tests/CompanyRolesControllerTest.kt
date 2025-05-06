package org.dataland.e2etests.tests

import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.communitymanager.openApiClient.model.CompanyRoleAssignment
import org.dataland.e2etests.REVIEWER_EXTENDED_ROLES
import org.dataland.e2etests.UPLOADER_EXTENDED_ROLES
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.CompanyRolesTestUtils
import org.dataland.e2etests.utils.DocumentControllerApiAccessor
import org.dataland.e2etests.utils.communityManager.assertAccessDeniedResponseBodyInCommunityManagerClientException
import org.dataland.e2etests.utils.communityManager.assertErrorCodeInCommunityManagerClientException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CompanyRolesControllerTest {
    private val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentControllerApiAccessor()
    val jwtHelper = JwtAuthenticationHelper()

    private val dataReaderUserId = UUID.fromString(TechnicalUser.Reader.technicalUserId)
    private val dataUploaderUserId = UUID.fromString(TechnicalUser.Uploader.technicalUserId)
    private val frameworkSampleData =
        apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getTData(1)[0]

    private val companyRolesTestUtils = CompanyRolesTestUtils()

    @BeforeAll
    fun postRequiredDummyDocuments() {
        documentManagerAccessor.uploadAllTestDocumentsAndAssurePersistence()
    }

    @Test
    fun `validate that creation and deletion of company role assignments works as expected`() {
        val firstCompanyId = companyRolesTestUtils.uploadCompanyAndReturnCompanyId()
        val secondCompanyId = companyRolesTestUtils.uploadCompanyAndReturnCompanyId()

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        companyRolesTestUtils.assignCompanyRole(CompanyRole.CompanyOwner, firstCompanyId, dataReaderUserId)

        val owners = companyRolesTestUtils.getCompanyRoleAssignments(CompanyRole.CompanyOwner, companyId = firstCompanyId)
        companyRolesTestUtils.validateCompanyOwnersForCompany(firstCompanyId, listOf(dataReaderUserId), owners)
        assertDoesNotThrow { companyRolesTestUtils.hasUserCompanyRole(CompanyRole.CompanyOwner, firstCompanyId, dataReaderUserId) }

        companyRolesTestUtils.assignCompanyRole(CompanyRole.CompanyOwner, firstCompanyId, dataReaderUserId)

        val ownersAfterRepost = companyRolesTestUtils.getCompanyRoleAssignments(CompanyRole.CompanyOwner, companyId = firstCompanyId)
        companyRolesTestUtils.validateCompanyOwnersForCompany(firstCompanyId, listOf(dataReaderUserId), ownersAfterRepost)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        companyRolesTestUtils.assertAccessDeniedWhenUploadingFrameworkData(secondCompanyId, frameworkSampleData, false)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        companyRolesTestUtils.removeCompanyRole(CompanyRole.CompanyOwner, firstCompanyId, dataReaderUserId)

        val ownersAfterDeletion = companyRolesTestUtils.getCompanyRoleAssignments(CompanyRole.CompanyOwner, companyId = firstCompanyId)
        companyRolesTestUtils.validateCompanyOwnersForCompany(firstCompanyId, listOf(), ownersAfterDeletion)
        val exceptionWhenCheckingIfUserIsCompanyOwner =
            assertThrows<ClientException> {
                companyRolesTestUtils.hasUserCompanyRole(CompanyRole.CompanyOwner, firstCompanyId, dataReaderUserId)
            }
        assertErrorCodeInCommunityManagerClientException(exceptionWhenCheckingIfUserIsCompanyOwner, 404)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        companyRolesTestUtils.assertAccessDeniedWhenUploadingFrameworkData(firstCompanyId, frameworkSampleData, false)
    }

    @Test
    fun `assure that users without keycloak admin role can always find out their role of a company`() {
        val companyId = companyRolesTestUtils.uploadCompanyAndReturnCompanyId()

        enumValues<CompanyRole>().forEach {
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
            companyRolesTestUtils.assignCompanyRole(it, companyId, dataReaderUserId)

            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            assertDoesNotThrow { companyRolesTestUtils.hasUserCompanyRole(it, companyId, dataReaderUserId) }

            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
            companyRolesTestUtils.removeCompanyRole(it, companyId, dataReaderUserId)
        }
    }

    @Test
    fun `check that accessing company ownership endpoints with an unknown companyId results in exceptions`() {
        val nonExistingCompanyId = UUID.randomUUID()

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val exceptionWhenPostingCompanyOwner =
            assertThrows<ClientException> {
                companyRolesTestUtils.assignCompanyRole(CompanyRole.CompanyOwner, nonExistingCompanyId, dataReaderUserId)
            }
        companyRolesTestUtils.assertCompanyNotFoundResponseBodyInCommunityManagerClientException(
            exceptionWhenPostingCompanyOwner,
            nonExistingCompanyId,
        )

        val exceptionWhenGettingCompanyOwners =
            assertThrows<ClientException> {
                companyRolesTestUtils.getCompanyRoleAssignments(CompanyRole.CompanyOwner, companyId = nonExistingCompanyId)
            }
        companyRolesTestUtils.assertCompanyNotFoundResponseBodyInCommunityManagerClientException(
            exceptionWhenGettingCompanyOwners,
            nonExistingCompanyId,
        )

        val exceptionWhenDeletingCompanyOwner =
            assertThrows<ClientException> {
                companyRolesTestUtils.removeCompanyRole(CompanyRole.CompanyOwner, nonExistingCompanyId, dataReaderUserId)
            }
        companyRolesTestUtils.assertCompanyNotFoundResponseBodyInCommunityManagerClientException(
            exceptionWhenDeletingCompanyOwner,
            nonExistingCompanyId,
        )

        val exceptionWhenCheckingIfUserIsCompanyOwner =
            assertThrows<ClientException> {
                companyRolesTestUtils.hasUserCompanyRole(CompanyRole.CompanyOwner, nonExistingCompanyId, dataReaderUserId)
            }
        assertErrorCodeInCommunityManagerClientException(exceptionWhenCheckingIfUserIsCompanyOwner, 404)
    }

    @Test
    fun `check that company ownership endpoints deny access if unauthorized or not sufficient rights`() {
        val companyId = companyRolesTestUtils.uploadCompanyAndReturnCompanyId()

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val postCompanyOwnerExceptionBecauseOfMissingRights =
            assertThrows<ClientException> {
                companyRolesTestUtils.assignCompanyRole(CompanyRole.CompanyOwner, companyId, dataReaderUserId)
            }
        assertAccessDeniedResponseBodyInCommunityManagerClientException(postCompanyOwnerExceptionBecauseOfMissingRights)

        val deleteExceptionBecauseOfMissingRights =
            assertThrows<ClientException> {
                companyRolesTestUtils.removeCompanyRole(CompanyRole.CompanyOwner, companyId, dataReaderUserId)
            }
        assertAccessDeniedResponseBodyInCommunityManagerClientException(deleteExceptionBecauseOfMissingRights)

        val expectedClientExceptionWhenCallingHeadEndpoint =
            assertThrows<ClientException> {
                companyRolesTestUtils.hasUserCompanyRole(CompanyRole.CompanyOwner, companyId, dataReaderUserId)
            }
        assertErrorCodeInCommunityManagerClientException(expectedClientExceptionWhenCallingHeadEndpoint, 403)

        val expectedClientExceptionWhenCallingGetCompanyOwnersEndpoint =
            assertThrows<ClientException> {
                companyRolesTestUtils.getCompanyRoleAssignments(CompanyRole.CompanyOwner, companyId = companyId)
            }
        assertErrorCodeInCommunityManagerClientException(
            expectedClientExceptionWhenCallingGetCompanyOwnersEndpoint,
            403,
        )
    }

    @Test
    fun `assure bypassQa is only allowed for user with keycloak uploader and keycloak reviewer rights`() {
        val companyId = companyRolesTestUtils.uploadCompanyAndReturnCompanyId()
        assertTrue(REVIEWER_EXTENDED_ROLES.size == 1 || UPLOADER_EXTENDED_ROLES.size == 1)

        for (technicalUser in TechnicalUser.entries) {
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(technicalUser)
            val isUserKeycloakReviewer = technicalUser.roles.contains(REVIEWER_EXTENDED_ROLES.first())
            val isUserKeycloakUploader = technicalUser.roles.contains(UPLOADER_EXTENDED_ROLES.first())
            if (isUserKeycloakReviewer && isUserKeycloakUploader) {
                assertDoesNotThrow { companyRolesTestUtils.uploadEuTaxoDataWithBypassQa(companyId) }
            } else {
                companyRolesTestUtils.assertAccessDeniedWhenUploadingFrameworkData(companyId, frameworkSampleData, true)
            }
        }
    }

    @Test
    fun `assure bypassQa for keycloak reader user is only allowed as company owner or company data uploader`() {
        val companyId = companyRolesTestUtils.uploadCompanyAndReturnCompanyId()

        val companyRolesAllowedToUploadData = listOf(CompanyRole.CompanyOwner, CompanyRole.DataUploader)
        for (role in CompanyRole.entries) {
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
            companyRolesTestUtils.assignCompanyRole(role, companyId, dataReaderUserId)

            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            if (companyRolesAllowedToUploadData.contains(role)) {
                assertDoesNotThrow { companyRolesTestUtils.uploadEuTaxoDataWithBypassQa(companyId) }
            } else {
                companyRolesTestUtils.assertAccessDeniedWhenUploadingFrameworkData(companyId, frameworkSampleData, true)
            }
        }
    }

    @Test
    fun `assure that the sheer existence of a company owner can be found out even by unauthorized users`() {
        val companyId = companyRolesTestUtils.uploadCompanyAndReturnCompanyId()

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        companyRolesTestUtils.assignCompanyRole(CompanyRole.CompanyOwner, companyId, dataReaderUserId)

        companyRolesTestUtils.removeBearerTokenFromApiClients()
        assertDoesNotThrow { companyRolesTestUtils.hasCompanyAtLeastOneOwner(companyId) }

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        assertDoesNotThrow { companyRolesTestUtils.removeCompanyRole(CompanyRole.CompanyOwner, companyId, dataReaderUserId) }

        companyRolesTestUtils.removeBearerTokenFromApiClients()

        val headExceptionForNonExistingCompanyOwners =
            assertThrows<ClientException> {
                apiAccessor.companyRolesControllerApi.hasCompanyAtLeastOneOwner(companyId)
            }
        assertErrorCodeInCommunityManagerClientException(headExceptionForNonExistingCompanyOwners, 404)
    }

    @Test
    fun `assure that a company owner without keycloak admin role can modify assignments for all company roles`() {
        val companyId = companyRolesTestUtils.uploadCompanyAndReturnCompanyId()
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        companyRolesTestUtils.assignCompanyRole(CompanyRole.CompanyOwner, companyId, dataReaderUserId)

        enumValues<CompanyRole>().forEach {
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            companyRolesTestUtils.assignCompanyRole(it, companyId, dataUploaderUserId)

            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
            assertDoesNotThrow { companyRolesTestUtils.hasUserCompanyRole(it, companyId, dataUploaderUserId) }

            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            companyRolesTestUtils.removeCompanyRole(it, companyId, dataUploaderUserId)

            val exceptionWhenCheckingIfUserIsCompanyOwner =
                assertThrows<ClientException> {
                    companyRolesTestUtils.hasUserCompanyRole(it, companyId, dataUploaderUserId)
                }
            assertErrorCodeInCommunityManagerClientException(exceptionWhenCheckingIfUserIsCompanyOwner, 404)
        }
    }

    @Test
    fun `assure that company member admin without keycloak admin role can only modify member and member admin roles`() {
        val companyId = companyRolesTestUtils.uploadCompanyAndReturnCompanyId()
        val rolesThatCanBeModified = listOf(CompanyRole.MemberAdmin, CompanyRole.Member)
        val rolesThatCannotBeModified =
            listOf(CompanyRole.CompanyOwner, CompanyRole.DataUploader)
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        companyRolesTestUtils.assignCompanyRole(CompanyRole.MemberAdmin, companyId, dataReaderUserId)
        rolesThatCanBeModified.forEach {
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            companyRolesTestUtils.assignCompanyRole(it, companyId, dataUploaderUserId)

            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
            assertDoesNotThrow { companyRolesTestUtils.hasUserCompanyRole(it, companyId, dataUploaderUserId) }

            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            companyRolesTestUtils.removeCompanyRole(it, companyId, dataUploaderUserId)

            val exceptionWhenCheckingIfUserIsCompanyOwner =
                assertThrows<ClientException> {
                    companyRolesTestUtils.hasUserCompanyRole(it, companyId, dataUploaderUserId)
                }
            assertErrorCodeInCommunityManagerClientException(exceptionWhenCheckingIfUserIsCompanyOwner, 404)
        }

        rolesThatCannotBeModified.forEach {
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            val exceptionWhenTryingToAddCompanyMembers =
                assertThrows<ClientException> {
                    companyRolesTestUtils.assignCompanyRole(it, companyId, dataUploaderUserId)
                }
            assertErrorCodeInCommunityManagerClientException(exceptionWhenTryingToAddCompanyMembers, 403)
        }
    }

    @Test
    fun `assure that a user with no role or only member or uploader company role can not modify role assignments`() {
        val companyId = companyRolesTestUtils.uploadCompanyAndReturnCompanyId()
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        companyRolesTestUtils.tryToAssignAndRemoveCompanyMembersAndAssertThatItsForbidden(companyId)

        val companyRolesWithoutModificationRights =
            listOf(CompanyRole.DataUploader, CompanyRole.Member)

        companyRolesWithoutModificationRights.forEach {
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
            companyRolesTestUtils.assignCompanyRole(it, companyId, dataReaderUserId)
            companyRolesTestUtils.tryToAssignAndRemoveCompanyMembersAndAssertThatItsForbidden(companyId)
        }
    }

    @Test
    fun `assure that user with assigned company role can access get and head endpoint but users without cant`() {
        val companyIdAlpha = companyRolesTestUtils.uploadCompanyAndReturnCompanyId()
        val companyIdBeta = companyRolesTestUtils.uploadCompanyAndReturnCompanyId()
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        companyRolesTestUtils.assignCompanyRole(CompanyRole.DataUploader, companyIdAlpha, dataUploaderUserId)
        enumValues<CompanyRole>().forEach {
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
            companyRolesTestUtils.assignCompanyRole(it, companyIdAlpha, dataReaderUserId)
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)

            assertDoesNotThrow { companyRolesTestUtils.getCompanyRoleAssignments(CompanyRole.Member, companyId = companyIdAlpha) }
            assertDoesNotThrow { companyRolesTestUtils.hasUserCompanyRole(CompanyRole.DataUploader, companyIdAlpha, dataUploaderUserId) }

            companyRolesTestUtils.tryToUseCompanyRoleGetAndHeadEndpointAndAsserThatItsForbidden(companyIdBeta)

            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
            companyRolesTestUtils.removeCompanyRole(it, companyIdAlpha, dataReaderUserId)
        }
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val exceptionWhenTryingToGetCompanyRoles =
            assertThrows<ClientException> {
                companyRolesTestUtils.getCompanyRoleAssignments(CompanyRole.Member, companyId = companyIdAlpha)
            }
        assertErrorCodeInCommunityManagerClientException(exceptionWhenTryingToGetCompanyRoles, 403)
        val exceptionWhenTryingToCheckCompanyRoles =
            assertThrows<ClientException> {
                companyRolesTestUtils.hasUserCompanyRole(CompanyRole.DataUploader, companyIdAlpha, dataUploaderUserId)
            }
        assertErrorCodeInCommunityManagerClientException(exceptionWhenTryingToCheckCompanyRoles, 403)
    }

    @Test
    fun `assure that existing role assignments are deleted when a user gets assigned a new company role`() {
        var currentAssignments: List<CompanyRoleAssignment>
        val companyId = companyRolesTestUtils.uploadCompanyAndReturnCompanyId()

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        companyRolesTestUtils.assignCompanyRole(CompanyRole.DataUploader, companyId, dataUploaderUserId)

        currentAssignments = companyRolesTestUtils.getCompanyRoleAssignments(companyId = companyId, userId = dataUploaderUserId)
        assertEquals(1, currentAssignments.size)
        assertEquals(
            CompanyRoleAssignment(
                companyRole = CompanyRole.DataUploader,
                companyId = companyId.toString(),
                userId = dataUploaderUserId.toString(),
            ),
            currentAssignments.first(),
        )

        companyRolesTestUtils.assignCompanyRole(CompanyRole.Member, companyId, dataUploaderUserId)

        currentAssignments = companyRolesTestUtils.getCompanyRoleAssignments(companyId = companyId, userId = dataUploaderUserId)
        assertEquals(1, currentAssignments.size)
        assertEquals(
            CompanyRoleAssignment(
                companyRole = CompanyRole.Member,
                companyId = companyId.toString(),
                userId = dataUploaderUserId.toString(),
            ),
            currentAssignments.first(),
        )
    }
}
