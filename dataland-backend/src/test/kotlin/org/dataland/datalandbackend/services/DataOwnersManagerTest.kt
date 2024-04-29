package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyDataOwnersEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.repositories.DataOwnerRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackend.services.messaging.DataOwnershipEmailMessageSender
import org.dataland.datalandbackend.services.messaging.DataOwnershipSuccessfullyEmailMessageSender
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandcommunitymanager.openApiClient.api.RequestControllerApi
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.*

class DataOwnersManagerTest {

    lateinit var dataOwnersManager: DataOwnersManager
    lateinit var mockDataOwnersRepository: DataOwnerRepository
    lateinit var mockCompanyRepository: StoredCompanyRepository
    lateinit var dataOwnershipSuccessfullyEmailMessageSender: DataOwnershipSuccessfullyEmailMessageSender
    lateinit var requestControllerApi: RequestControllerApi

    private val testUserId = UUID.randomUUID().toString()
    private val testCompanyName = "Test Company AG"
    private val mockAuthentication = AuthenticationMock.mockJwtAuthentication(
        "username",
        testUserId,
        setOf(DatalandRealmRole.ROLE_USER),
    )

    @BeforeEach
    fun initializeDataOwnersManager() {
        mockDataOwnersRepository = mock(DataOwnerRepository::class.java)
        mockCompanyRepository = mock(StoredCompanyRepository::class.java)
        dataOwnershipSuccessfullyEmailMessageSender = mock(DataOwnershipSuccessfullyEmailMessageSender::class.java)
        requestControllerApi = mock(RequestControllerApi::class.java)
        dataOwnersManager = DataOwnersManager(
            mockDataOwnersRepository,
            mockCompanyRepository,
            mock(DataOwnershipEmailMessageSender::class.java),
            dataOwnershipSuccessfullyEmailMessageSender,
        )

        Mockito.doNothing().`when`(dataOwnershipSuccessfullyEmailMessageSender)
            .sendDataOwnershipAcceptanceExternalEmailMessage(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
            )
    }

    @Test
    fun `check that a data ownership can only be requested for existing companies`() {
        `when`(mockCompanyRepository.findById(any())).thenReturn(Optional.empty())
        val exception = assertThrows<ResourceNotFoundApiException> {
            dataOwnersManager.checkCompanyForDataOwnership(
                "non-existing-company-id",
            )
        }
        assertTrue(exception.summary.contains("Company is invalid"))
    }

    @Test
    fun `check that a data ownership can only be requested if the user is not already a data owner`() {
        val mockStoredCompany = mock(StoredCompanyEntity::class.java)
        `when`(mockStoredCompany.companyName).thenReturn(testCompanyName)
        `when`(mockCompanyRepository.findById(any())).thenReturn(Optional.of(mockStoredCompany))
        `when`(mockDataOwnersRepository.findById(any())).thenReturn(
            Optional.of(
                CompanyDataOwnersEntity(
                    "indeed-existing-company-id",
                    mutableListOf(testUserId),
                ),
            ),
        )
        val exception = assertThrows<InvalidInputApiException> {
            dataOwnersManager.sendDataOwnershipRequestIfNecessary(
                "indeed-existing-company-id",
                mockAuthentication,
                null,
                "",
            )
        }
        assertTrue(exception.summary.contains("User is already a data owner for company."))
    }

    @Test
    fun `check that email for users becoming company data owner is not generated if company does not exist`() {
        val mockStoredCompany = mock(StoredCompanyEntity::class.java)

        `when`(mockStoredCompany.companyName).thenReturn(testCompanyName)
        `when`(mockStoredCompany.companyId).thenReturn(UUID.randomUUID().toString())

        val exception = assertThrows<ResourceNotFoundApiException> {
            dataOwnersManager.addDataOwnerToCompany(
                companyId = mockStoredCompany.companyId,
                userId = testUserId,
                companyName = mockStoredCompany.companyName,
            )
        }
        assertTrue(exception.summary.contains("Company is invalid"))
    }

    @Test
    fun `check that email generated for users becoming company data owner are generated`() {
        val mockStoredCompany = mock(StoredCompanyEntity::class.java)
        val companyId = UUID.randomUUID().toString()

        `when`(mockStoredCompany.companyName).thenReturn(testCompanyName)
        `when`(mockStoredCompany.companyId).thenReturn(companyId)
        `when`(mockCompanyRepository.existsById(mockStoredCompany.companyId)).thenReturn(true)
        `when`(mockDataOwnersRepository.existsById(mockStoredCompany.companyId)).thenReturn(false)
        `when`(
            dataOwnershipSuccessfullyEmailMessageSender.getNumberOfOpenDataRequestsForCompany(
                mockStoredCompany.companyId,
            ),
        ).thenReturn(5)
        `when`(dataOwnershipSuccessfullyEmailMessageSender.getEmailAddressDataOwner(testUserId))
            .thenReturn("test@test.com")
        val mockCompanyDataOwnersEntity = mock(CompanyDataOwnersEntity::class.java)
        `when`(mockDataOwnersRepository.save(any(CompanyDataOwnersEntity::class.java)))
            .thenReturn(mockCompanyDataOwnersEntity)

        dataOwnersManager.addDataOwnerToCompany(
            companyId = mockStoredCompany.companyId,
            userId = testUserId,
            companyName = mockStoredCompany.companyName,
        )

        Mockito.verify(dataOwnershipSuccessfullyEmailMessageSender, Mockito.times(1))
            .sendDataOwnershipAcceptanceExternalEmailMessage(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
            )
    }

    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
}
