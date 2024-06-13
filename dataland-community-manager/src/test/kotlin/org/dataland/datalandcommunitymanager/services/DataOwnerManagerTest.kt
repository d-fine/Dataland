package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalandbackend.services.messaging.DataOwnershipEmailMessageSender
import org.dataland.datalandbackend.services.messaging.DataOwnershipSuccessfullyEmailMessageSender
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandcommunitymanager.repositories.DataOwnerRepository
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

class DataOwnerManagerTest {

    lateinit var dataOwnerManager: DataOwnerManager
    lateinit var mockDataOwnersRepository: DataOwnerRepository
    lateinit var dataOwnershipSuccessfullyEmailMessageSender: DataOwnershipSuccessfullyEmailMessageSender
    lateinit var mockCompanyDataControllerApi: CompanyDataControllerApi

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
        dataOwnershipSuccessfullyEmailMessageSender = mock(DataOwnershipSuccessfullyEmailMessageSender::class.java)
        mockCompanyDataControllerApi = mock(CompanyDataControllerApi::class.java)
        dataOwnerManager = DataOwnerManager(
            mockCompanyDataControllerApi,
                    mockDataOwnersRepository,
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
        val         mockStoredCompany = mock(StoredCompany::class.java)

        `when`(mockCompanyDataControllerApi.getCompanyById(any())).thenReturn(mockStoredCompany)
        val exception = assertThrows<ResourceNotFoundApiException> {
            dataOwnerManager.checkCompanyForDataOwnership(
                "non-existing-company-id",
            )
        }
        assertTrue(exception.summary.contains("Company is invalid"))
    }
/*
    @Test
    fun `check that a data ownership can only be requested if the user is not already a data owner`() {
        val         mockStoredCompany = mock(StoredCompany::class.java)
        `when`(mockStoredCompany.companyInformation.companyName).thenReturn(testCompanyName)
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
            dataOwnerManager.sendDataOwnershipRequestIfNecessary(
                "indeed-existing-company-id",
                mockAuthentication,
                null,
                "",
            )
        }
        assertTrue(exception.summary.contains("User is already a data owner for company."))
    } TODO later*/

    @Test
    fun `check that email for users becoming company data owner is not generated if company does not exist`() {
        val exception = assertThrows<ResourceNotFoundApiException> {
            dataOwnerManager.addDataOwnerToCompany(
                companyId = UUID.randomUUID().toString(),
                userId = testUserId,
                companyName = testCompanyName,
            )
        }
        assertTrue(exception.summary.contains("Company is invalid"))
    }
/*
    @Test
    fun `check that email generated for users becoming company data owner are generated`() {
        val companyId = UUID.randomUUID().toString()

        `when`(mockCompanyRepository.existsById(companyId)).thenReturn(true)
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
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
            )
    } TODO later */

    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
}
