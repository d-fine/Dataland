package org.dataland.e2etests.tests

import org.dataland.communitymanager.openApiClient.infrastructure.ClientError
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.communitymanager.openApiClient.model.CompanyRoleAssignment
import org.dataland.datalandbackend.openApiClient.model.EutaxonomyNonFinancialsData
import org.dataland.e2etests.REVIEWER_EXTENDED_ROLES
import org.dataland.e2etests.UPLOADER_EXTENDED_ROLES
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentManagerAccessor
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
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException as CommunityManagerClientException
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException as BackendClientException

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CompanyRolesControllerTest {
    private val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentManagerAccessor()
    val jwtHelper = JwtAuthenticationHelper()

    private val dataReaderUserId = UUID.fromString(TechnicalUser.Reader.technicalUserId)
    private val dataUploaderUserId = UUID.fromString(TechnicalUser.Uploader.technicalUserId)
    private val frameworkSampleData = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getTData(1)[0]

    @BeforeAll
    fun postRequiredDummyDocuments() {
        documentManagerAccessor.uploadAllTestDocumentsAndAssurePersistence()
    }

    private fun removeBearerTokenFromApiClients() {
        GlobalAuth.setBearerToken(null)
    }

    private fun validateCompanyOwnersForCompany(
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
        expectedCompanyOwnerUserIds.map { it.toString() }
            .forEach { expectedUserId -> assertTrue(actualCompanyOwnerUserIds.contains(expectedUserId)) }
    }

    private fun assertAccessDeniedWhenUploadingFrameworkData(
        companyId: UUID,
        dataSet: EutaxonomyNonFinancialsData,
        bypassQa: Boolean = false,
    ) {
        val reportingPeriod = "2022"
        val expectedAccessDeniedClientException = assertThrows<BackendClientException> {
            apiAccessor.euTaxonomyNonFinancialsUploaderFunction(
                companyId.toString(),
                dataSet,
                reportingPeriod,
                bypassQa,
            )
        }
        assertEquals("Client error : 403 ", expectedAccessDeniedClientException.message)
    }

    private fun uploadEuTaxoDataWithBypassQa(companyId: UUID) {
        apiAccessor.euTaxonomyNonFinancialsUploaderFunction(companyId.toString(), frameworkSampleData, "2021", true)
    }

    private fun assertCompanyNotFoundResponseBodyInCommunityManagerClientException(
        communityManagerClientException: CommunityManagerClientException,
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

    private fun assignCompanyRole(companyRole: CompanyRole, companyId: UUID, userId: UUID): CompanyRoleAssignment {
        return apiAccessor.companyRolesControllerApi.assignCompanyRole(companyRole, companyId, userId)
    }

    private fun getCompanyRoleAssignments(
        companyRole: CompanyRole? = null,
        companyId: UUID? = null,
        userId: UUID? = null,
    ): List<CompanyRoleAssignment> {
        return apiAccessor.companyRolesControllerApi.getCompanyRoleAssignments(companyRole, companyId, userId)
    }

    private fun removeCompanyRole(companyRole: CompanyRole, companyId: UUID, userId: UUID) {
        apiAccessor.companyRolesControllerApi.removeCompanyRole(companyRole, companyId, userId)
    }

    private fun hasUserCompanyRole(companyRole: CompanyRole, companyId: UUID, userId: UUID) {
        apiAccessor.companyRolesControllerApi.hasUserCompanyRole(companyRole, companyId, userId)
    }

    private fun hasCompanyAtLeastOneOwner(companyId: UUID) {
        apiAccessor.companyRolesControllerApi.hasCompanyAtLeastOneOwner(companyId)
    }

    private fun uploadCompanyAndReturnCompanyId(): UUID {
        return UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )
    }

    @Test
    fun `validate that creation and deletion of company role assignments works as expected`() {
        val firstCompanyId = uploadCompanyAndReturnCompanyId()
        val secondCompanyId = uploadCompanyAndReturnCompanyId()

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        assignCompanyRole(CompanyRole.CompanyOwner, firstCompanyId, dataReaderUserId)

        val owners = getCompanyRoleAssignments(CompanyRole.CompanyOwner, companyId = firstCompanyId)
        validateCompanyOwnersForCompany(firstCompanyId, listOf(dataReaderUserId), owners)
        assertDoesNotThrow { hasUserCompanyRole(CompanyRole.CompanyOwner, firstCompanyId, dataReaderUserId) }

        assignCompanyRole(CompanyRole.CompanyOwner, firstCompanyId, dataReaderUserId)

        val ownersAfterRepost = getCompanyRoleAssignments(CompanyRole.CompanyOwner, companyId = firstCompanyId)
        validateCompanyOwnersForCompany(firstCompanyId, listOf(dataReaderUserId), ownersAfterRepost)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        assertAccessDeniedWhenUploadingFrameworkData(secondCompanyId, frameworkSampleData, false)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        removeCompanyRole(CompanyRole.CompanyOwner, firstCompanyId, dataReaderUserId)

        val ownersAfterDeletion = getCompanyRoleAssignments(CompanyRole.CompanyOwner, companyId = firstCompanyId)
        validateCompanyOwnersForCompany(firstCompanyId, listOf(), ownersAfterDeletion)
        val exceptionWhenCheckingIfUserIsCompanyOwner = assertThrows<ClientException> {
            hasUserCompanyRole(CompanyRole.CompanyOwner, firstCompanyId, dataReaderUserId)
        }
        assertErrorCodeInCommunityManagerClientException(exceptionWhenCheckingIfUserIsCompanyOwner, 404)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        assertAccessDeniedWhenUploadingFrameworkData(firstCompanyId, frameworkSampleData, false)
    }

    @Test
    fun `assure that users without keycloak admin role can always find out their role of a company`() {
        val companyId = uploadCompanyAndReturnCompanyId()

        enumValues<CompanyRole>().forEach {
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
            assignCompanyRole(it, companyId, dataReaderUserId)

            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            assertDoesNotThrow { hasUserCompanyRole(it, companyId, dataReaderUserId) }

            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
            removeCompanyRole(it, companyId, dataReaderUserId)
        }
    }

    @Test
    fun `check that accessing company ownership endpoints with an unknown companyId results in exceptions`() {
        val nonExistingCompanyId = UUID.randomUUID()

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val exceptionWhenPostingCompanyOwner = assertThrows<ClientException> {
            assignCompanyRole(CompanyRole.CompanyOwner, nonExistingCompanyId, dataReaderUserId)
        }
        assertCompanyNotFoundResponseBodyInCommunityManagerClientException(
            exceptionWhenPostingCompanyOwner,
            nonExistingCompanyId,
        )

        val exceptionWhenGettingCompanyOwners = assertThrows<ClientException> {
            getCompanyRoleAssignments(CompanyRole.CompanyOwner, companyId = nonExistingCompanyId)
        }
        assertCompanyNotFoundResponseBodyInCommunityManagerClientException(
            exceptionWhenGettingCompanyOwners,
            nonExistingCompanyId,
        )

        val exceptionWhenDeletingCompanyOwner = assertThrows<ClientException> {
            removeCompanyRole(CompanyRole.CompanyOwner, nonExistingCompanyId, dataReaderUserId)
        }
        assertCompanyNotFoundResponseBodyInCommunityManagerClientException(
            exceptionWhenDeletingCompanyOwner,
            nonExistingCompanyId,
        )

        val exceptionWhenCheckingIfUserIsCompanyOwner = assertThrows<ClientException> {
            hasUserCompanyRole(CompanyRole.CompanyOwner, nonExistingCompanyId, dataReaderUserId)
        }
        assertErrorCodeInCommunityManagerClientException(exceptionWhenCheckingIfUserIsCompanyOwner, 404)
    }

    @Test
    fun `check that company ownership endpoints deny access if unauthorized or not sufficient rights`() {
        val companyId = uploadCompanyAndReturnCompanyId()

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val postCompanyOwnerExceptionBecauseOfMissingRights = assertThrows<ClientException> {
            assignCompanyRole(CompanyRole.CompanyOwner, companyId, dataReaderUserId)
        }
        assertAccessDeniedResponseBodyInCommunityManagerClientException(postCompanyOwnerExceptionBecauseOfMissingRights)

        val deleteExceptionBecauseOfMissingRights = assertThrows<ClientException> {
            removeCompanyRole(CompanyRole.CompanyOwner, companyId, dataReaderUserId)
        }
        assertAccessDeniedResponseBodyInCommunityManagerClientException(deleteExceptionBecauseOfMissingRights)

        val expectedClientExceptionWhenCallingHeadEndpoint = assertThrows<ClientException> {
            hasUserCompanyRole(CompanyRole.CompanyOwner, companyId, dataReaderUserId)
        }
        assertErrorCodeInCommunityManagerClientException(expectedClientExceptionWhenCallingHeadEndpoint, 403)

        val expectedClientExceptionWhenCallingGetCompanyOwnersEndpoint = assertThrows<ClientException> {
            getCompanyRoleAssignments(CompanyRole.CompanyOwner, companyId = companyId)
        }
        assertErrorCodeInCommunityManagerClientException(
            expectedClientExceptionWhenCallingGetCompanyOwnersEndpoint,
            403,
        )
    }

    @Test
    fun `assure bypassQa is only allowed for user with keycloak uploader and keycloak reviewer rights`() {
        val companyId = uploadCompanyAndReturnCompanyId()
        assertTrue(REVIEWER_EXTENDED_ROLES.size == 1 || UPLOADER_EXTENDED_ROLES.size == 1)

        for (technicalUser in TechnicalUser.values()) {
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(technicalUser)
            val isUserKeycloakReviewer = technicalUser.roles.contains(REVIEWER_EXTENDED_ROLES.first())
            val isUserKeycloakUploader = technicalUser.roles.contains(UPLOADER_EXTENDED_ROLES.first())
            if (isUserKeycloakReviewer && isUserKeycloakUploader) {
                assertDoesNotThrow { uploadEuTaxoDataWithBypassQa(companyId) }
            } else {
                assertAccessDeniedWhenUploadingFrameworkData(companyId, frameworkSampleData, true)
            }
        }
    }

    @Test
    fun `assure bypassQa for keycloak reader user is only allowed as company owner or company data uploader`() {
        val companyId = uploadCompanyAndReturnCompanyId()

        val companyRolesAllowedToUploadData = listOf(CompanyRole.CompanyOwner, CompanyRole.DataUploader)
        for (role in CompanyRole.values()) {
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
            assignCompanyRole(role, companyId, dataReaderUserId)

            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            if (companyRolesAllowedToUploadData.contains(role)) {
                assertDoesNotThrow { uploadEuTaxoDataWithBypassQa(companyId) }
            } else {
                assertAccessDeniedWhenUploadingFrameworkData(companyId, frameworkSampleData, true)
            }
        }
    }

    @Test
    fun `assure that the sheer existence of a company owner can be found out even by unauthorized users`() {
        val companyId = uploadCompanyAndReturnCompanyId()

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        assignCompanyRole(CompanyRole.CompanyOwner, companyId, dataReaderUserId)

        removeBearerTokenFromApiClients()
        assertDoesNotThrow { hasCompanyAtLeastOneOwner(companyId) }

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        assertDoesNotThrow { removeCompanyRole(CompanyRole.CompanyOwner, companyId, dataReaderUserId) }

        removeBearerTokenFromApiClients()

        val headExceptionForNonExistingCompanyOwners = assertThrows<ClientException> {
            apiAccessor.companyRolesControllerApi.hasCompanyAtLeastOneOwner(companyId)
        }
        assertErrorCodeInCommunityManagerClientException(headExceptionForNonExistingCompanyOwners, 404)
    }

    @Test
    fun `assure that a company owner without keycloak admin role can modify assignments for all company roles`() {
        val companyId = uploadCompanyAndReturnCompanyId()
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        assignCompanyRole(CompanyRole.CompanyOwner, companyId, dataReaderUserId)

        enumValues<CompanyRole>().forEach {
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            assignCompanyRole(it, companyId, dataUploaderUserId)

            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
            assertDoesNotThrow { hasUserCompanyRole(it, companyId, dataUploaderUserId) }

            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            removeCompanyRole(it, companyId, dataUploaderUserId)

            val exceptionWhenCheckingIfUserIsCompanyOwner = assertThrows<ClientException> {
                hasUserCompanyRole(it, companyId, dataUploaderUserId)
            }
            assertErrorCodeInCommunityManagerClientException(exceptionWhenCheckingIfUserIsCompanyOwner, 404)
        }
    }

    @Test
    fun `assure that company member admin without keycloak admin role can only modify member and member admin roles`() {
        val companyId = uploadCompanyAndReturnCompanyId()
        val rolesThatCanBeModified = listOf(CompanyRole.MemberAdmin, CompanyRole.Member)
        val rolesThatCannotBeModified =
            listOf(CompanyRole.CompanyOwner, CompanyRole.DataUploader)
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        assignCompanyRole(CompanyRole.MemberAdmin, companyId, dataReaderUserId)
        rolesThatCanBeModified.forEach {
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            assignCompanyRole(it, companyId, dataUploaderUserId)

            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
            assertDoesNotThrow { hasUserCompanyRole(it, companyId, dataUploaderUserId) }

            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            removeCompanyRole(it, companyId, dataUploaderUserId)

            val exceptionWhenCheckingIfUserIsCompanyOwner = assertThrows<ClientException> {
                hasUserCompanyRole(it, companyId, dataUploaderUserId)
            }
            assertErrorCodeInCommunityManagerClientException(exceptionWhenCheckingIfUserIsCompanyOwner, 404)
        }

        rolesThatCannotBeModified.forEach {
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            val exceptionWhenTryingToAddCompanyMembers = assertThrows<ClientException> {
                assignCompanyRole(it, companyId, dataUploaderUserId)
            }
            assertErrorCodeInCommunityManagerClientException(exceptionWhenTryingToAddCompanyMembers, 403)
        }
    }

    @Test
    fun `assure that a user with no role or only member or uploader company role can not modify role assignments`() {
        val companyId = uploadCompanyAndReturnCompanyId()
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        tryToAssignAndRemoveCompanyMembersAndAssertThatItsForbidden(companyId)

        val companyRolesWithoutModificationRights =
            listOf(CompanyRole.DataUploader, CompanyRole.Member)

        companyRolesWithoutModificationRights.forEach {
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
            assignCompanyRole(it, companyId, dataReaderUserId)
            tryToAssignAndRemoveCompanyMembersAndAssertThatItsForbidden(companyId)
        }
    }

    @Test
    fun `assure that user with assigned company role can access get and head endpoint but users without cant`() {
        val companyIdAlpha = uploadCompanyAndReturnCompanyId()
        val companyIdBeta = uploadCompanyAndReturnCompanyId()
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        assignCompanyRole(CompanyRole.DataUploader, companyIdAlpha, dataUploaderUserId)
        enumValues<CompanyRole>().forEach {
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
            assignCompanyRole(it, companyIdAlpha, dataReaderUserId)
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)

            assertDoesNotThrow { getCompanyRoleAssignments(CompanyRole.Member, companyId = companyIdAlpha) }
            assertDoesNotThrow { hasUserCompanyRole(CompanyRole.DataUploader, companyIdAlpha, dataUploaderUserId) }

            tryToUseCompanyRoleGetAndHeadEndpointAndAsserThatItsForbidden(companyIdBeta)

            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
            removeCompanyRole(it, companyIdAlpha, dataReaderUserId)
        }
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val exceptionWhenTryingToGetCompanyRoles = assertThrows<ClientException> {
            getCompanyRoleAssignments(CompanyRole.Member, companyId = companyIdAlpha)
        }
        assertErrorCodeInCommunityManagerClientException(exceptionWhenTryingToGetCompanyRoles, 403)
        val exceptionWhenTryingToCheckCompanyRoles = assertThrows<ClientException> {
            hasUserCompanyRole(CompanyRole.DataUploader, companyIdAlpha, dataUploaderUserId)
        }
        assertErrorCodeInCommunityManagerClientException(exceptionWhenTryingToCheckCompanyRoles, 403)
    }

    @Test
    fun `assure that existing role assignments are deleted when a user gets assigned a new company role`() {
        var currentAssignments: List<CompanyRoleAssignment>
        val companyId = uploadCompanyAndReturnCompanyId()

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        assignCompanyRole(CompanyRole.DataUploader, companyId, dataUploaderUserId)

        currentAssignments = getCompanyRoleAssignments(companyId = companyId, userId = dataUploaderUserId)
        assertEquals(1, currentAssignments.size)
        assertEquals(
            CompanyRoleAssignment(
                companyRole = CompanyRole.DataUploader,
                companyId = companyId.toString(),
                userId = dataUploaderUserId.toString(),
            ),
            currentAssignments.first(),
        )

        assignCompanyRole(CompanyRole.Member, companyId, dataUploaderUserId)

        currentAssignments = getCompanyRoleAssignments(companyId = companyId, userId = dataUploaderUserId)
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

    private fun tryToUseCompanyRoleGetAndHeadEndpointAndAsserThatItsForbidden(companyId: UUID) {
        val exceptionWhenGettingCompanyRolesForAnotherCompany = assertThrows<ClientException> {
            getCompanyRoleAssignments(CompanyRole.Member, companyId = companyId)
        }
        assertErrorCodeInCommunityManagerClientException(exceptionWhenGettingCompanyRolesForAnotherCompany, 403)
        val exceptionWhenCheckingCompanyRolesForAnotherCompany = assertThrows<ClientException> {
            hasUserCompanyRole(CompanyRole.DataUploader, companyId, dataUploaderUserId)
        }
        assertErrorCodeInCommunityManagerClientException(exceptionWhenCheckingCompanyRolesForAnotherCompany, 403)
    }
    private fun tryToAssignAndRemoveCompanyMembersAndAssertThatItsForbidden(companyId: UUID) {
        enumValues<CompanyRole>().forEach {
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            val exceptionWhenTryingToAddCompanyMembers = assertThrows<ClientException> {
                assignCompanyRole(it, companyId, dataUploaderUserId)
            }
            assertErrorCodeInCommunityManagerClientException(exceptionWhenTryingToAddCompanyMembers, 403)

            val exceptionWhenTryingToDeleteCompanyMembers = assertThrows<ClientException> {
                removeCompanyRole(it, companyId, dataUploaderUserId)
            }
            assertErrorCodeInCommunityManagerClientException(exceptionWhenTryingToDeleteCompanyMembers, 403)
        }
    }
}
