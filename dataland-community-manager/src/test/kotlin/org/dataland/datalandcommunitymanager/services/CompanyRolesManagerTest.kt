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
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
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
import java.io.File
import java.util.Optional

class CompanyRolesManagerTest {
    private lateinit var companyRolesManager: CompanyRolesManager

    private val mockCompanyInfoService = mock<CompanyInfoService>()
    private val mockCompanyRoleAssignmentRepository = mock<CompanyRoleAssignmentRepository>()
    private val mockCompanyOwnershipAcceptedEmailMessageBuilder = mock<CompanyOwnershipAcceptedEmailMessageBuilder>()
    private val mockCompanyOwnershipRequestedEmailMessageBuilder = mock<CompanyOwnershipRequestedEmailMessageBuilder>()
    private val mockKeycloakUserService = mock<KeycloakUserService>()

    private val objectMapper = JsonUtils.defaultObjectMapper

    private val filePathToTestFixtures = "./src/test/resources/companyRolesManager"

    private val dummyKeycloakUserInfo =
        objectMapper
            .readValue<KeycloakUserInfo>(File("$filePathToTestFixtures/dummyKeycloakUserInfo.json"))
    private val testUserId = dummyKeycloakUserInfo.userId

    private val deletedUserKeycloakUserInfo =
        objectMapper.readValue<KeycloakUserInfo>(File("$filePathToTestFixtures/deletedUserKeycloakUserInfo.json"))
    private val deletedUserId = deletedUserKeycloakUserInfo.userId

    private val dummyCompanyRoleAssignmentEntity =
        objectMapper.readValue<CompanyRoleAssignmentEntity>(
            File("$filePathToTestFixtures/dummyCompanyRoleAssignmentEntity.json"),
        )
    private val existingCompanyId = dummyCompanyRoleAssignmentEntity.companyId
    private val nonExistingCompanyId = "non-existing-company-id"
    private val testCompanyName = "Test Company AG"
    private val dummyCompanyRoleAssignment = dummyCompanyRoleAssignmentEntity.toApiModel()

    private val companyRoleAssignmentEntityOfDeletedUser =
        objectMapper.readValue<CompanyRoleAssignmentEntity>(
            File("$filePathToTestFixtures/companyRoleAssignmentEntityOfDeletedUser.json"),
        )
    private val companyRoleAssignmentOfDeletedUser =
        companyRoleAssignmentEntityOfDeletedUser.toApiModel()

    private val companyRoleAssignmentEntityList =
        listOf(dummyCompanyRoleAssignmentEntity, companyRoleAssignmentEntityOfDeletedUser)

    private val existingCompanyRoleAssignmentId =
        objectMapper.readValue<CompanyRoleAssignmentId>(
            File("$filePathToTestFixtures/existingCompanyRoleAssignmentId.json"),
        )

    private val nonExistingCompanyRoleAssignmentId =
        objectMapper.readValue<CompanyRoleAssignmentId>(
            File("$filePathToTestFixtures/nonExistingCompanyRoleAssignmentId.json"),
        )

    @BeforeEach
    fun setup() {
        reset(
            mockCompanyInfoService,
            mockCompanyRoleAssignmentRepository,
            mockCompanyOwnershipAcceptedEmailMessageBuilder,
            mockCompanyOwnershipRequestedEmailMessageBuilder,
            mockKeycloakUserService,
        )

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
                "Company not found",
                "Dataland does not know the company ID $nonExistingCompanyId",
            ),
        ).whenever(mockCompanyInfoService).assertCompanyIdIsValid(nonExistingCompanyId)

        doReturn(testCompanyName).whenever(mockCompanyInfoService).getValidCompanyName(existingCompanyId)
        doThrow(
            ResourceNotFoundApiException(
                "Company not found",
                "Dataland does not know the company ID $nonExistingCompanyId",
            ),
        ).whenever(mockCompanyInfoService).getValidCompanyName(nonExistingCompanyId)

        doReturn(dummyKeycloakUserInfo).whenever(mockKeycloakUserService).getUser(testUserId)
        doReturn(deletedUserKeycloakUserInfo).whenever(mockKeycloakUserService).getUser(deletedUserId)

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
        assertTrue(exception.summary.contains("Company not found"))
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
        assertTrue(exception.summary.contains("Company not found"))
    }

    @Test
    fun `check that email generated for users becoming company owner are generated`() {
        doReturn(false).whenever(mockCompanyRoleAssignmentRepository).existsById(existingCompanyRoleAssignmentId)
        doReturn(Optional.empty<CompanyRoleAssignmentEntity>())
            .whenever(mockCompanyRoleAssignmentRepository)
            .findById(existingCompanyRoleAssignmentId)

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
    fun `check that extension of company role assignments works as intended`() {
        val extendedCompanyRoleAssignments =
            companyRolesManager.convertToExtendedCompanyRoleAssignments(
                listOf(
                    dummyCompanyRoleAssignment, companyRoleAssignmentOfDeletedUser,
                ),
            )

        assertEquals(1, extendedCompanyRoleAssignments.size)
        assertEquals(
            CompanyRoleAssignmentExtended(
                companyRole = CompanyRole.CompanyOwner,
                companyId = existingCompanyId,
                userId = testUserId,
                email = "test@example.com",
                firstName = "Jane",
                lastName = "Doe",
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
            2,
            returnedCompanyRoleAssignmentEntities.size,
        )
        assert(returnedCompanyRoleAssignmentEntities.containsAll(companyRoleAssignmentEntityList))
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
        assertTrue(exception.summary.contains("Company not found"))
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
        assertTrue(exception.summary.contains("Company role is not assigned to user"))
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
        assertTrue(exception.summary.contains("Company not found"))
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
        assertTrue(exception.summary.contains("Company role is not assigned to user"))
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
