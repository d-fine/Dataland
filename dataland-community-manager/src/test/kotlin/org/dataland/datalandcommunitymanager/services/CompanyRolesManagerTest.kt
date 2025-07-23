package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandcommunitymanager.entities.CompanyRoleAssignmentEntity
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
import org.mockito.Mockito
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import java.util.UUID

class CompanyRolesManagerTest {
    private lateinit var companyRolesManager: CompanyRolesManager

    private lateinit var mockCompanyRoleAssignmentRepository: CompanyRoleAssignmentRepository
    private lateinit var mockCompanyRoleAssignmentEntity: CompanyRoleAssignmentEntity

    private lateinit var companyOwnershipAcceptedEmailMessageBuilder: CompanyOwnershipAcceptedEmailMessageBuilder

    private lateinit var mockCompanyDataControllerApi: CompanyDataControllerApi
    private lateinit var mockCompanyInfoService: CompanyInfoService

    private val testUserId = UUID.randomUUID().toString()

    private val testCompanyName = "Test Company AG"
    private val testCompanyInformation =
        CompanyInformation(
            companyName = testCompanyName,
            headquarters = "dummyHeadquarters",
            identifiers = emptyMap(),
            countryCode = "dummyCountryCode",
        )

    @BeforeEach
    fun initializeCompanyRolesManager() {
        mockCompanyRoleAssignmentRepository = mock(CompanyRoleAssignmentRepository::class.java)

        companyOwnershipAcceptedEmailMessageBuilder = mock(CompanyOwnershipAcceptedEmailMessageBuilder::class.java)
        `when`(
            companyOwnershipAcceptedEmailMessageBuilder
                .getNumberOfOpenDataRequestsForCompany(any(String::class.java)),
        ).thenReturn(5)

        mockCompanyDataControllerApi = mock(CompanyDataControllerApi::class.java)
        mockCompanyInfoService = CompanyInfoService(mockCompanyDataControllerApi)

        companyRolesManager =
            CompanyRolesManager(
                mockCompanyInfoService,
                mockCompanyRoleAssignmentRepository,
                mock(CompanyOwnershipRequestedEmailMessageBuilder::class.java),
                companyOwnershipAcceptedEmailMessageBuilder,
            )

        mockCompanyRoleAssignmentEntity = mock(CompanyRoleAssignmentEntity::class.java)
        `when`(mockCompanyRoleAssignmentRepository.save(any(CompanyRoleAssignmentEntity::class.java)))
            .thenReturn(mockCompanyRoleAssignmentEntity)

        doNothing()
            .`when`(companyOwnershipAcceptedEmailMessageBuilder)
            .buildCompanyOwnershipAcceptanceExternalEmailAndSendCEMessage(
                anyString(),
                anyString(), anyString(), anyString(),
            )
    }

    @Test
    fun `check that a company ownership can only be requested for existing companies`() {
        `when`(mockCompanyDataControllerApi.isCompanyIdValid("non-existing-company-id")).thenThrow(
            ClientException("Client error", HttpStatus.NOT_FOUND.value()),
        )
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
        val mockStoredCompany = mock(StoredCompany::class.java)
        val existingCompanyId = "indeed-existing-company-id"
        `when`(mockCompanyDataControllerApi.getCompanyById(existingCompanyId)).thenReturn(mockStoredCompany)
        `when`(mockStoredCompany.companyInformation).thenReturn(testCompanyInformation)

        val id =
            CompanyRoleAssignmentId(
                companyRole = CompanyRole.CompanyOwner,
                companyId = existingCompanyId,
                userId = testUserId,
            )
        `when`(mockCompanyRoleAssignmentRepository.existsById(id)).thenReturn(true)

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
    fun `check that email for users becoming company company owner is not generated if company does not exist`() {
        `when`(mockCompanyDataControllerApi.getCompanyById(anyString())).thenThrow(
            ClientException("Client error", HttpStatus.NOT_FOUND.value()),
        )
        val exception =
            assertThrows<ResourceNotFoundApiException> {
                companyRolesManager.assignCompanyRoleForCompanyToUser(
                    companyRole = CompanyRole.CompanyOwner,
                    companyId = UUID.randomUUID().toString(),
                    userIdentifier = testUserId,
                )
            }
        verifyNoInteractions(companyOwnershipAcceptedEmailMessageBuilder)
        assertTrue(exception.summary.contains("Company not found"))
    }

    @Test
    fun `check that email generated for users becoming company owner are generated`() {
        val companyId = UUID.randomUUID().toString()
        val storedCompany =
            StoredCompany(
                companyId = companyId,
                companyInformation = testCompanyInformation,
                dataRegisteredByDataland = listOf(),
            )
        `when`(mockCompanyDataControllerApi.getCompanyById(companyId)).thenReturn(storedCompany)

        val id =
            CompanyRoleAssignmentId(
                companyRole = CompanyRole.CompanyOwner,
                companyId = companyId,
                userId = testUserId,
            )
        `when`(mockCompanyRoleAssignmentRepository.existsById(id)).thenReturn(false)

        companyRolesManager.assignCompanyRoleForCompanyToUser(
            companyRole = CompanyRole.CompanyOwner,
            companyId = companyId,
            userIdentifier = testUserId,
        )

        Mockito
            .verify(companyOwnershipAcceptedEmailMessageBuilder, Mockito.times(1))
            .buildCompanyOwnershipAcceptanceExternalEmailAndSendCEMessage(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
            )
    }

    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
}
