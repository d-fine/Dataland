package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalandbackend.services.messaging.DataOwnershipEmailMessageSender
import org.dataland.datalandbackend.services.messaging.DataOwnershipSuccessfullyEmailMessageSender
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandcommunitymanager.entities.CompanyDataOwnersEntity
import org.dataland.datalandcommunitymanager.repositories.DataOwnerRepository
import org.dataland.datalandcommunitymanager.utils.CompanyIdValidator
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
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
import java.util.*

class DataOwnerManagerTest {

    lateinit var dataOwnerManager: DataOwnerManager
    lateinit var mockDataOwnersRepository: DataOwnerRepository
    lateinit var dataOwnershipSuccessfullyEmailMessageSender: DataOwnershipSuccessfullyEmailMessageSender
    lateinit var mockCompanyDataControllerApi: CompanyDataControllerApi
    lateinit var mockCompanyIdValidator: CompanyIdValidator

    private val testUserId = UUID.randomUUID().toString()

    private val testCompanyName = "Test Company AG"
    private val testCompanyInformation = CompanyInformation(
        companyName = testCompanyName,
        headquarters = "dummyHeadquarters",
        identifiers = emptyMap(),
        countryCode = "dummyCountryCode",
    )
    private val mockAuthentication = AuthenticationMock.mockJwtAuthentication(
        "username",
        testUserId,
        setOf(DatalandRealmRole.ROLE_USER),
    )

    @BeforeEach
    fun initializeDataOwnersManager() {
        mockDataOwnersRepository = mock(DataOwnerRepository::class.java)
        dataOwnershipSuccessfullyEmailMessageSender = mock(DataOwnershipSuccessfullyEmailMessageSender::class.java)
        mockCompanyDataControllerApi = mock(CompanyDataControllerApi::class.java)
        mockCompanyIdValidator = CompanyIdValidator(mockCompanyDataControllerApi)
        dataOwnerManager = DataOwnerManager(
            mockCompanyDataControllerApi,
            mockCompanyIdValidator,
            mockDataOwnersRepository,
            mock(DataOwnershipEmailMessageSender::class.java),
            dataOwnershipSuccessfullyEmailMessageSender,
        )

        doNothing().`when`(dataOwnershipSuccessfullyEmailMessageSender)
            .sendDataOwnershipAcceptanceExternalEmailMessage(
                anyString(),
                anyString(), anyString(), anyString(),
            )
    }

    @Test
    fun `check that a data ownership can only be requested for existing companies`() {
        `when`(mockCompanyDataControllerApi.getCompanyById("non-existing-company-id")).thenThrow(
            ClientException("Client error", HttpStatus.NOT_FOUND.value()),
        )
        val exception = assertThrows<ResourceNotFoundApiException> {
            dataOwnerManager.checkCompanyForDataOwnership(
                "non-existing-company-id",
            )
        }
        assertTrue(exception.summary.contains("Company not found"))
    }

    @Test
    fun `check that a data ownership can only be requested if the user is not already a data owner`() {
        val mockStoredCompany = mock(StoredCompany::class.java)
        val existingCompanyId = "indeed-existing-company-id"
        `when`(mockStoredCompany.companyInformation).thenReturn(testCompanyInformation)
        `when`(mockCompanyDataControllerApi.getCompanyById(existingCompanyId)).thenReturn(mockStoredCompany)
        `when`(mockDataOwnersRepository.findById(existingCompanyId)).thenReturn(
            Optional.of(
                CompanyDataOwnersEntity(
                    existingCompanyId,
                    mutableListOf(testUserId),
                ),
            ),
        )
        val exception = assertThrows<InvalidInputApiException> {
            dataOwnerManager.sendDataOwnershipRequestIfNecessary(
                existingCompanyId,
                mockAuthentication,
                null,
                "",
            )
        }
        assertTrue(exception.summary.contains("User is already a data owner for company."))
    }

    @Test
    fun `check that email for users becoming company data owner is not generated if company does not exist`() {
        `when`(mockCompanyDataControllerApi.getCompanyById(anyString())).thenThrow(
            ClientException("Client error", HttpStatus.NOT_FOUND.value()),
        )
        val exception = assertThrows<ResourceNotFoundApiException> {
            dataOwnerManager.addDataOwnerToCompany(
                companyId = UUID.randomUUID().toString(),
                userId = testUserId,
                companyName = testCompanyName,
            )
        }
        verifyNoInteractions(dataOwnershipSuccessfullyEmailMessageSender)
        assertTrue(exception.summary.contains("Company not found"))
    }

    @Test
    fun `check that email generated for users becoming company data owner are generated`() {
        val companyId = UUID.randomUUID().toString()
        `when`(mockCompanyIdValidator.checkIfCompanyIdIsValid(companyId)).thenReturn(null)
        `when`(mockDataOwnersRepository.existsById(companyId)).thenReturn(false)
        `when`(
            dataOwnershipSuccessfullyEmailMessageSender.getNumberOfOpenDataRequestsForCompany(
                companyId,
            ),
        ).thenReturn(5)
        `when`(dataOwnershipSuccessfullyEmailMessageSender.getEmailAddressDataOwner(testUserId))
            .thenReturn("test@example.com")
        val mockCompanyDataOwnersEntity = mock(CompanyDataOwnersEntity::class.java)
        `when`(mockDataOwnersRepository.save(any(CompanyDataOwnersEntity::class.java)))
            .thenReturn(mockCompanyDataOwnersEntity)

        dataOwnerManager.addDataOwnerToCompany(
            companyId = companyId,
            userId = testUserId,
            companyName = testCompanyName,
        )

        Mockito.verify(dataOwnershipSuccessfullyEmailMessageSender, Mockito.times(1))
            .sendDataOwnershipAcceptanceExternalEmailMessage(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
            )
    }

    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
}
