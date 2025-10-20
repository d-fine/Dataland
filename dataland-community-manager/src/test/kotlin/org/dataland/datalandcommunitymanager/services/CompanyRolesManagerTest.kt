package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.dataland.datalandcommunitymanager.entities.CompanyRoleAssignmentEntity
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRoleAssignment
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRoleAssignmentExtended
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRoleAssignmentId
import org.dataland.datalandcommunitymanager.repositories.CompanyRoleAssignmentRepository
import org.dataland.datalandcommunitymanager.services.messaging.CompanyOwnershipAcceptedEmailMessageBuilder
import org.dataland.datalandcommunitymanager.services.messaging.CompanyOwnershipRequestedEmailMessageBuilder
import org.dataland.datalandcommunitymanager.utils.CompanyInfoService
import org.dataland.datalandcommunitymanager.utils.TestUtils
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.io.File
import java.util.Optional

class CompanyRolesManagerTest {
    private lateinit var companyRolesManager: CompanyRolesManager

    private val mockCompanyInfoService = mock<CompanyInfoService>()
    private val mockCompanyRoleAssignmentRepository = mock<CompanyRoleAssignmentRepository>()
    private val mockCompanyOwnershipAcceptedEmailMessageBuilder = mock<CompanyOwnershipAcceptedEmailMessageBuilder>()
    private val mockCompanyOwnershipRequestedEmailMessageBuilder = mock<CompanyOwnershipRequestedEmailMessageBuilder>()
    private val mockDatalandJwtAuthentication = mock<DatalandJwtAuthentication>()
    private val mockSecurityContext = mock<SecurityContext>()
    private val mockKeycloakUserService = mock<KeycloakUserService>()

    private val objectMapper = JsonUtils.defaultObjectMapper

    private val filePathToTestFixtures = "./src/test/resources/companyRolesManager"

    private val dummyKeycloakUserInfo =
        objectMapper
            .readValue<KeycloakUserInfo>(File("$filePathToTestFixtures/dummyKeycloakUserInfo.json"))
    private val testUserId = dummyKeycloakUserInfo.userId
    private val testEmail = dummyKeycloakUserInfo.email
    private val testFirstName = dummyKeycloakUserInfo.firstName
    private val testLastName = dummyKeycloakUserInfo.lastName

    private val nonExistingUserId = "non-existing-user-id"

    private val dummyCompanyRoleAssignmentEntity =
        objectMapper.readValue<CompanyRoleAssignmentEntity>(
            File("$filePathToTestFixtures/dummyCompanyRoleAssignmentEntity.json"),
        )
    private val existingCompanyId = dummyCompanyRoleAssignmentEntity.companyId
    private val nonExistingCompanyId = "non-existing-company-id"
    private val testCompanyName = "Test Company AG"
    private val dummyCompanyRoleAssignment = dummyCompanyRoleAssignmentEntity.toApiModel()

    private val companyRoleAssignmentEntityList =
        listOf(dummyCompanyRoleAssignmentEntity)

    private val existingCompanyRoleAssignmentId =
        objectMapper.readValue<CompanyRoleAssignmentId>(
            File("$filePathToTestFixtures/existingCompanyRoleAssignmentId.json"),
        )

    private val nonExistingCompanyRoleAssignmentId =
        objectMapper.readValue<CompanyRoleAssignmentId>(
            File("$filePathToTestFixtures/nonExistingCompanyRoleAssignmentId.json"),
        )

    private val companyNotFound = "Company not found"
    private val companyIdNotKnown = "Dataland does not know the company ID $nonExistingCompanyId"
    private val unknownUserId = "Unknown user ID"
    private val companyRoleNotAssigned = "Company role is not assigned to user"

    @BeforeEach
    fun resetMocks() {
        reset(
            mockCompanyInfoService,
            mockCompanyRoleAssignmentRepository,
            mockCompanyOwnershipAcceptedEmailMessageBuilder,
            mockCompanyOwnershipRequestedEmailMessageBuilder,
            mockDatalandJwtAuthentication,
            mockSecurityContext,
            mockKeycloakUserService,
        )
    }

    @BeforeEach
    fun setupMocks() {
        doReturn(5)
            .whenever(mockCompanyOwnershipAcceptedEmailMessageBuilder)
            .getNumberOfOpenDataRequestsForCompany(anyString())

        doAnswer { invocation -> invocation.arguments[0] }.whenever(mockCompanyRoleAssignmentRepository).save(any())

        doNothing()
            .whenever(mockCompanyOwnershipAcceptedEmailMessageBuilder)
            .buildCompanyOwnershipAcceptanceExternalEmailAndSendCEMessage(
                anyString(), anyString(), anyString(), anyString(),
            )

        doNothing().whenever(mockCompanyInfoService).assertCompanyIdIsValid(existingCompanyId)
        doThrow(
            ResourceNotFoundApiException(
                companyNotFound,
                companyIdNotKnown,
            ),
        ).whenever(mockCompanyInfoService).assertCompanyIdIsValid(nonExistingCompanyId)

        doReturn(testCompanyName).whenever(mockCompanyInfoService).getValidCompanyName(existingCompanyId)
        doThrow(
            ResourceNotFoundApiException(
                companyNotFound,
                companyIdNotKnown,
            ),
        ).whenever(mockCompanyInfoService).getValidCompanyName(nonExistingCompanyId)

        doReturn(dummyKeycloakUserInfo).whenever(mockKeycloakUserService).getUser(testUserId)
        doReturn(true).whenever(mockKeycloakUserService).isKeycloakUserId(testUserId)
        doReturn(false).whenever(mockKeycloakUserService).isKeycloakUserId(nonExistingUserId)

        doReturn(companyRoleAssignmentEntityList)
            .whenever(mockCompanyRoleAssignmentRepository)
            .getCompanyRoleAssignmentsByProvidedParameters(
                companyRole = null, companyId = existingCompanyId, userId = null,
            )

        doReturn(Optional.empty<CompanyRoleAssignment>())
            .whenever(mockCompanyRoleAssignmentRepository)
            .findById(nonExistingCompanyRoleAssignmentId)

        doReturn(false)
            .whenever(mockCompanyRoleAssignmentRepository)
            .existsById(nonExistingCompanyRoleAssignmentId)
        doReturn(true)
            .whenever(mockCompanyRoleAssignmentRepository)
            .existsById(existingCompanyRoleAssignmentId)
    }

    @BeforeEach
    fun initializeCompanyRolesManager() {
        companyRolesManager =
            CompanyRolesManager(
                mockCompanyInfoService,
                mockCompanyRoleAssignmentRepository,
                mockCompanyOwnershipRequestedEmailMessageBuilder,
                mockCompanyOwnershipAcceptedEmailMessageBuilder,
                mockKeycloakUserService,
            )
    }

    @Test
    fun `check that a company ownership can only be requested for existing companies`() {
        val exception =
            assertThrows<ResourceNotFoundApiException> {
                companyRolesManager.validateIfCompanyHasAtLeastOneCompanyOwner(
                    "non-existing-company-id",
                )
            }
        assertEquals(companyNotFound, exception.summary)
    }

    @Test
    fun `check that a company ownership can only be requested if the user is not already a company owner`() {
        doReturn(true).whenever(mockCompanyRoleAssignmentRepository).existsById(existingCompanyRoleAssignmentId)

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
        val exception =
            assertThrows<ResourceNotFoundApiException> {
                companyRolesManager.assignCompanyRoleForCompanyToUser(
                    companyRole = CompanyRole.CompanyOwner,
                    companyId = nonExistingCompanyId,
                    userId = testUserId,
                )
            }
        verifyNoInteractions(mockCompanyOwnershipAcceptedEmailMessageBuilder)
        assertEquals(companyNotFound, exception.summary)
    }

    @Test
    fun `check that email generated for users becoming company owner are generated`() {
        doReturn(false).whenever(mockCompanyRoleAssignmentRepository).existsById(existingCompanyRoleAssignmentId)

        companyRolesManager.assignCompanyRoleForCompanyToUser(
            companyRole = CompanyRole.CompanyOwner,
            companyId = existingCompanyId,
            userId = testUserId,
        )

        verify(mockCompanyOwnershipAcceptedEmailMessageBuilder, times(1))
            .buildCompanyOwnershipAcceptanceExternalEmailAndSendCEMessage(
                anyString(), anyString(), anyString(), anyString(),
            )
    }

    @Test
    fun `check that an unauthenticated user is not considered owner or admin of any company`() {
        doReturn(null).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)

        assertFalse(companyRolesManager.currentUserIsOwnerOrAdminOfAtLeastOneCompany())
    }

    @ParameterizedTest
    @EnumSource(value = CompanyRole::class)
    fun `check that the different company roles pass or fail the authorization check for user lookup by email as appropriate`(
        companyRole: CompanyRole,
    ) {
        val companyOwnerUserId = "user-id-of-company-owner"
        doReturn(companyOwnerUserId).whenever(mockDatalandJwtAuthentication).userId
        doReturn(mockDatalandJwtAuthentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)
        doReturn(
            listOf(
                CompanyRoleAssignmentEntity(
                    companyRole = companyRole,
                    companyId = "dummy-company-id",
                    userId = companyOwnerUserId,
                ),
            ),
        ).whenever(mockCompanyRoleAssignmentRepository)
            .getCompanyRoleAssignmentsByProvidedParameters(
                companyId = null,
                userId = companyOwnerUserId,
                companyRole = null,
            )

        if (companyRole in listOf(CompanyRole.CompanyOwner, CompanyRole.MemberAdmin)) {
            assertTrue(companyRolesManager.currentUserIsOwnerOrAdminOfAtLeastOneCompany())
        } else {
            assertFalse(companyRolesManager.currentUserIsOwnerOrAdminOfAtLeastOneCompany())
        }
    }

    @Test
    fun `check that assignment of company roles does not work for unknown user IDs`() {
        val exception =
            assertThrows<ResourceNotFoundApiException> {
                companyRolesManager.assignCompanyRoleForCompanyToUser(
                    companyRole = CompanyRole.DataUploader,
                    companyId = existingCompanyId,
                    userId = nonExistingUserId,
                )
            }

        assertEquals(unknownUserId, exception.summary)
    }

    @Test
    fun `check that extension of company role assignments works as intended`() {
        val extendedCompanyRoleAssignments =
            companyRolesManager.convertToExtendedCompanyRoleAssignments(
                listOf(dummyCompanyRoleAssignment),
            )

        assertEquals(1, extendedCompanyRoleAssignments.size)
        assertEquals(
            CompanyRoleAssignmentExtended(
                companyRole = CompanyRole.CompanyOwner,
                companyId = existingCompanyId,
                userId = testUserId,
                email = testEmail!!,
                firstName = testFirstName,
                lastName = testLastName,
            ),
            extendedCompanyRoleAssignments.first(),
        )
    }

    @Test
    fun `check that retrieval of company roles does not work if company does not exist`() {
        assertThrows<ResourceNotFoundApiException> {
            companyRolesManager.getCompanyRoleAssignmentsByParameters(
                companyRole = null, companyId = nonExistingCompanyId, userId = null,
            )
        }
    }

    @Test
    fun `check that retrieval of company roles works if company does exist`() {
        val returnedCompanyRoleAssignmentEntities =
            assertDoesNotThrow {
                companyRolesManager.getCompanyRoleAssignmentsByParameters(
                    companyRole = null, companyId = existingCompanyId, userId = null,
                )
            }

        assertEquals(
            1,
            returnedCompanyRoleAssignmentEntities.size,
        )
        assertEquals(
            dummyCompanyRoleAssignmentEntity,
            returnedCompanyRoleAssignmentEntities.first(),
        )
    }

    @Test
    fun `check that removal of company role does not work if company does not exist`() {
        val exception =
            assertThrows<ResourceNotFoundApiException> {
                companyRolesManager.removeCompanyRoleForCompanyFromUser(
                    companyRole = CompanyRole.DataUploader,
                    companyId = nonExistingCompanyId,
                    userId = testUserId,
                )
            }
        assertEquals(companyNotFound, exception.summary)
    }

    @Test
    fun `check that removal of company role does not work if user does not have the role in the company`() {
        val exception =
            assertThrows<ResourceNotFoundApiException> {
                companyRolesManager.removeCompanyRoleForCompanyFromUser(
                    companyRole = CompanyRole.DataUploader,
                    companyId = existingCompanyId,
                    userId = testUserId,
                )
            }
        assertEquals(companyRoleNotAssigned, exception.summary)
    }

    @Test
    fun `check that removal of company role works as intended for an existing company role`() {
        doReturn(Optional.of(dummyCompanyRoleAssignmentEntity))
            .whenever(mockCompanyRoleAssignmentRepository)
            .findById(existingCompanyRoleAssignmentId)

        assertDoesNotThrow {
            companyRolesManager.removeCompanyRoleForCompanyFromUser(
                companyRole = CompanyRole.CompanyOwner,
                companyId = existingCompanyId,
                userId = testUserId,
            )
        }

        verify(mockCompanyRoleAssignmentRepository, times(1)).deleteById(existingCompanyRoleAssignmentId)
    }

    @Test
    fun `check that validation of company role works as intended if company does not exist`() {
        val exception =
            assertThrows<ResourceNotFoundApiException> {
                companyRolesManager.validateIfCompanyRoleForCompanyIsAssignedToUser(
                    companyRole = CompanyRole.DataUploader,
                    companyId = nonExistingCompanyId,
                    userId = testUserId,
                )
            }
        assertEquals(companyNotFound, exception.summary)
    }

    @Test
    fun `check that validation of company role works as intended if company role does not exist`() {
        val exception =
            assertThrows<ResourceNotFoundApiException> {
                companyRolesManager.validateIfCompanyRoleForCompanyIsAssignedToUser(
                    companyRole = CompanyRole.DataUploader,
                    companyId = existingCompanyId,
                    userId = testUserId,
                )
            }
        assertEquals(companyRoleNotAssigned, exception.summary)
    }

    @Test
    fun `check that validation of company role works as intended if company role exists`() {
        assertDoesNotThrow {
            companyRolesManager.validateIfCompanyRoleForCompanyIsAssignedToUser(
                companyRole = CompanyRole.CompanyOwner,
                companyId = existingCompanyId,
                userId = testUserId,
            )
        }
    }
}
