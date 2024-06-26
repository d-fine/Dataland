package org.dataland.e2etests.tests

import org.dataland.communitymanager.openApiClient.infrastructure.ClientError
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.communitymanager.openApiClient.model.CompanyRoleAssignment
import org.dataland.datalandbackend.openApiClient.model.EutaxonomyNonFinancialsData
import org.dataland.e2etests.READER_USER_ID
import org.dataland.e2etests.UPLOADER_USER_ID
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentManagerAccessor
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

    private val dataReaderUserId = UUID.fromString(READER_USER_ID)
    private val dataUploaderUserId = UUID.fromString(UPLOADER_USER_ID)
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

    private fun uploadEuTaxoData(companyId: UUID, dataSet: EutaxonomyNonFinancialsData) {
        val reportingPeriod = "2022"
        apiAccessor.euTaxonomyNonFinancialsUploaderFunction(
            companyId.toString(),
            dataSet,
            reportingPeriod,
            false,
        )
    }

    private fun assertErrorCodeInCommunityManagerClientException(
        communityManagerClientException: CommunityManagerClientException,
        expectedErrorCode: Number,
    ) {
        assertEquals("Client error : $expectedErrorCode ", communityManagerClientException.message)
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

    private fun assertAccessDeniedResponseBodyInCommunityManagerClientException(
        communityManagerClientException: CommunityManagerClientException,
    ) {
        assertErrorCodeInCommunityManagerClientException(communityManagerClientException, 403)
        val responseBody = (communityManagerClientException.response as ClientError<*>).body as String
        assertTrue(responseBody.contains("Access Denied"))
    }

    private fun assignCompanyRole(companyRole: CompanyRole, companyId: UUID, userId: UUID): CompanyRoleAssignment {
        return apiAccessor.companyRolesControllerApi.assignCompanyRole(companyRole, companyId, userId)
    }

    private fun getCompanyRoleAssignments(companyRole: CompanyRole, companyId: UUID): List<CompanyRoleAssignment> {
        return apiAccessor.companyRolesControllerApi.getCompanyRoleAssignments(companyRole, companyId)
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
    fun `check that company ownership enables a user with only reader rights to upload data`() {
        val firstCompanyId = uploadCompanyAndReturnCompanyId()
        val secondCompanyId = uploadCompanyAndReturnCompanyId()

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        assertAccessDeniedWhenUploadingFrameworkData(firstCompanyId, frameworkSampleData, false)
        assertAccessDeniedWhenUploadingFrameworkData(secondCompanyId, frameworkSampleData, false)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        assignCompanyRole(CompanyRole.CompanyOwner, firstCompanyId, dataReaderUserId)
        val owners = getCompanyRoleAssignments(CompanyRole.CompanyOwner, firstCompanyId)
        validateCompanyOwnersForCompany(firstCompanyId, listOf(dataReaderUserId), owners)
        assertDoesNotThrow { hasUserCompanyRole(CompanyRole.CompanyOwner, firstCompanyId, dataReaderUserId) }

        assignCompanyRole(CompanyRole.CompanyOwner, firstCompanyId, dataReaderUserId)
        val ownersAfterRepost = getCompanyRoleAssignments(CompanyRole.CompanyOwner, firstCompanyId)
        validateCompanyOwnersForCompany(firstCompanyId, listOf(dataReaderUserId), ownersAfterRepost)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        uploadEuTaxoData(firstCompanyId, frameworkSampleData)
        assertAccessDeniedWhenUploadingFrameworkData(secondCompanyId, frameworkSampleData, false)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        removeCompanyRole(CompanyRole.CompanyOwner, firstCompanyId, dataReaderUserId)
        val ownersAfterDeletion = getCompanyRoleAssignments(CompanyRole.CompanyOwner, firstCompanyId)
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
            getCompanyRoleAssignments(CompanyRole.CompanyOwner, nonExistingCompanyId)
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
            getCompanyRoleAssignments(CompanyRole.CompanyOwner, companyId)
        }
        assertErrorCodeInCommunityManagerClientException(
            expectedClientExceptionWhenCallingGetCompanyOwnersEndpoint,
            403,
        )
    }

    @Test
    fun `assure that bypassQa is forbidden for users even if they are a company owner`() {
        val companyId = uploadCompanyAndReturnCompanyId()

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        assignCompanyRole(CompanyRole.CompanyOwner, companyId, dataReaderUserId)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        assertAccessDeniedWhenUploadingFrameworkData(companyId, frameworkSampleData, true)

        uploadEuTaxoData(companyId, frameworkSampleData)
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
    fun `assure that a company owner without keycloak admin role can add and remove every company roles`() {
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
    fun `assure that company member admin without keycloak admin role can only change member and member admin roles`() {
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
    fun `assure that every company role has access to get and head endpoint but not a user without any company role`() {
        val companyId = uploadCompanyAndReturnCompanyId()
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        assignCompanyRole(CompanyRole.DataUploader, companyId, dataUploaderUserId)
        enumValues<CompanyRole>().forEach {
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
            assignCompanyRole(it, companyId, dataReaderUserId)

            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            assertDoesNotThrow {
                getCompanyRoleAssignments(CompanyRole.Member, companyId)
            }

            assertDoesNotThrow {
                hasUserCompanyRole(CompanyRole.DataUploader, companyId, dataUploaderUserId)
            }

            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
            removeCompanyRole(it, companyId, dataReaderUserId)
        }
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val exceptionWhenTryingToGetCompanyRoles = assertThrows<ClientException> {
            getCompanyRoleAssignments(CompanyRole.Member, companyId)
        }
        assertErrorCodeInCommunityManagerClientException(exceptionWhenTryingToGetCompanyRoles, 403)
        val exceptionWhenTryingToCheckCompanyRoles = assertThrows<ClientException> {
            hasUserCompanyRole(CompanyRole.DataUploader, companyId, dataUploaderUserId)
        }
        assertErrorCodeInCommunityManagerClientException(exceptionWhenTryingToCheckCompanyRoles, 403)
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
