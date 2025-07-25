package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRoleAssignmentId
import org.dataland.datalandcommunitymanager.repositories.CompanyRoleAssignmentRepository
import org.dataland.datalandcommunitymanager.services.messaging.CompanyOwnershipAcceptedEmailMessageBuilder
import org.dataland.datalandcommunitymanager.services.messaging.CompanyOwnershipRequestedEmailMessageBuilder
import org.dataland.datalandcommunitymanager.utils.CompanyInfoService
import org.dataland.datalandcommunitymanager.utils.TestUtils
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class CompanyRolesManagerTest {
    private lateinit var companyRolesManager: CompanyRolesManager

    private val mockCompanyInfoService = mock<CompanyInfoService>()
    private val mockCompanyRoleAssignmentRepository = mock<CompanyRoleAssignmentRepository>()
    private val mockCompanyOwnershipAcceptedEmailMessageBuilder = mock<CompanyOwnershipAcceptedEmailMessageBuilder>()
    private val mockCompanyOwnershipRequestedEmailMessageBuilder = mock<CompanyOwnershipRequestedEmailMessageBuilder>()
    private val mockKeycloakUserService = mock<KeycloakUserService>()

    private val existingCompanyId = "indeed-existing-company-id"
    private val nonExistingCompanyId = "non-existing-company-id"
    private val testUserId = UUID.randomUUID().toString()
    private val testCompanyName = "Test Company AG"

    @BeforeEach
    fun initializeCompanyRolesManager() {
        doReturn(5)
            .whenever(mockCompanyOwnershipAcceptedEmailMessageBuilder)
            .getNumberOfOpenDataRequestsForCompany(anyString())

        companyRolesManager =
            CompanyRolesManager(
                mockCompanyInfoService,
                mockCompanyRoleAssignmentRepository,
                mockCompanyOwnershipRequestedEmailMessageBuilder,
                mockCompanyOwnershipAcceptedEmailMessageBuilder,
                mockKeycloakUserService,
            )

        doAnswer { invocation -> invocation.arguments[0] }.whenever(mockCompanyRoleAssignmentRepository).save(any())
        doNothing()
            .whenever(mockCompanyOwnershipAcceptedEmailMessageBuilder)
            .buildCompanyOwnershipAcceptanceExternalEmailAndSendCEMessage(
                anyString(), anyString(), anyString(), anyString(),
            )
    }

    @Test
    fun `check that a company ownership can only be requested for existing companies`() {
        doThrow(
            ResourceNotFoundApiException(
                "Company not found",
                "Dataland does not know the company ID $nonExistingCompanyId",
            ),
        ).whenever(mockCompanyInfoService).assertCompanyIdIsValid(nonExistingCompanyId)

        val exception =
            assertThrows<ResourceNotFoundApiException> {
                companyRolesManager.validateIfCompanyHasAtLeastOneCompanyOwner(
                    "non-existing-company-id",
                )
            }
        assertTrue(exception.summary.contains("Company not found"))
    }

    @Test
    fun `check that a company ownership can only be requested if the user is not already a company owner`() {
        doReturn(testCompanyName).whenever(mockCompanyInfoService).getValidCompanyName(existingCompanyId)

        val id =
            CompanyRoleAssignmentId(
                companyRole = CompanyRole.CompanyOwner,
                companyId = existingCompanyId,
                userId = testUserId,
            )
        doReturn(true).whenever(mockCompanyRoleAssignmentRepository).existsById(id)

        val mockAuthentication = TestUtils.mockSecurityContext("username", testUserId, DatalandRealmRole.ROLE_USER)
        val exception =
            assertThrows<InvalidInputApiException> {
                companyRolesManager.triggerCompanyOwnershipRequest(
                    existingCompanyId,
                    mockAuthentication,
                    null,
                    "",
                )
            }
        assertTrue(exception.summary.contains("User is already a company owner for company."))
    }

    @Test
    fun `check that email for users becoming company owner is not generated if company does not exist`() {
        doThrow(
            ResourceNotFoundApiException(
                "Company not found",
                "Dataland does not know the company ID $nonExistingCompanyId",
            ),
        ).whenever(mockCompanyInfoService).getValidCompanyName(nonExistingCompanyId)
        val exception =
            assertThrows<ResourceNotFoundApiException> {
                companyRolesManager.assignCompanyRoleForCompanyToUser(
                    companyRole = CompanyRole.CompanyOwner,
                    companyId = nonExistingCompanyId,
                    userIdentifier = testUserId,
                )
            }
        verifyNoInteractions(mockCompanyOwnershipAcceptedEmailMessageBuilder)
        assertTrue(exception.summary.contains("Company not found"))
    }

    @Test
    fun `check that email generated for users becoming company owner are generated`() {
        doReturn(testCompanyName).whenever(mockCompanyInfoService).getValidCompanyName(existingCompanyId)

        val id =
            CompanyRoleAssignmentId(
                companyRole = CompanyRole.CompanyOwner,
                companyId = existingCompanyId,
                userId = testUserId,
            )
        doReturn(false).whenever(mockCompanyRoleAssignmentRepository).existsById(id)

        companyRolesManager.assignCompanyRoleForCompanyToUser(
            companyRole = CompanyRole.CompanyOwner,
            companyId = existingCompanyId,
            userIdentifier = testUserId,
        )

        verify(mockCompanyOwnershipAcceptedEmailMessageBuilder, times(1))
            .buildCompanyOwnershipAcceptanceExternalEmailAndSendCEMessage(
                anyString(), anyString(), anyString(), anyString(),
            )
    }
}
