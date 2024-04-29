package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyDataOwnersEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.repositories.DataOwnerRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackend.services.messaging.DataOwnershipEmailMessageSender
import org.dataland.datalandbackend.services.messaging.DataOwnershipSuccessfullyEmailMessageSender
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.rmi.ServerException
import java.util.*

class DataOwnersManagerTest {

    lateinit var dataOwnersManager: DataOwnersManager
    lateinit var mockDataOwnersRepository: DataOwnerRepository
    lateinit var mockCompanyRepository: StoredCompanyRepository
    lateinit var dataOwnershipSuccessfullyEmailMessageSender: DataOwnershipSuccessfullyEmailMessageSender

    private val testUserId = UUID.randomUUID().toString()
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
        dataOwnersManager = DataOwnersManager(
            mockDataOwnersRepository,
            mockCompanyRepository,
            mock(DataOwnershipEmailMessageSender::class.java),
            dataOwnershipSuccessfullyEmailMessageSender,
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
        `when`(mockStoredCompany.companyName).thenReturn("Weihnachtsmann & Co. KG")
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
    fun `check that email generated for users becoming company data owner are generated`() {
        val mockStoredCompany = mock(StoredCompanyEntity::class.java)
        val correlationId = UUID.randomUUID().toString()

        `when`(mockStoredCompany.companyName).thenReturn("Weihnachtsmann & Co. KG")
        `when`(mockStoredCompany.companyId).thenReturn(UUID.randomUUID().toString())

        try {
            dataOwnershipSuccessfullyEmailMessageSender.sendDataOwnershipAcceptanceExternalEmailMessage(
                testUserId,
                mockStoredCompany.companyId,
                mockStoredCompany.companyName,
                correlationId,
            )
        } catch (error: ServerException) {
            println("ServerException was thrown")
            throw error
        }
    }
}
