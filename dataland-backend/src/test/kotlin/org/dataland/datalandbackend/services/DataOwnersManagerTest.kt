package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyDataOwnersEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.repositories.DataOwnerRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackend.services.messaging.EmailMessageSender
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
import java.util.*

class DataOwnersManagerTest {

    lateinit var dataOwnersManager: DataOwnersManager
    lateinit var mockDataOwnersRepository: DataOwnerRepository
    lateinit var mockCompanyRepository: StoredCompanyRepository

    @BeforeEach
    fun initializeDataOwnersManager() {
        mockDataOwnersRepository = mock(DataOwnerRepository::class.java)
        mockCompanyRepository = mock(StoredCompanyRepository::class.java)
        dataOwnersManager = DataOwnersManager(
            mockDataOwnersRepository,
            mockCompanyRepository,
            mock(EmailMessageSender::class.java),
        )
    }

    @Test
    fun `check that a data ownership can only be requested for existing companies`() {
        `when`(mockCompanyRepository.findById(any())).thenReturn(Optional.empty())
        val exception = assertThrows<ResourceNotFoundApiException> {
            dataOwnersManager.sendDataOwnershipRequestIfNecessary(
                "non-existing-company-id",
                mockAuthentication,
                null,
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
                    mutableListOf("user-id"),
                ),
            ),
        )
        val exception = assertThrows<InvalidInputApiException> {
            dataOwnersManager.sendDataOwnershipRequestIfNecessary(
                "indeed-existing-company-id",
                mockAuthentication,
                null,
            )
        }
        assertTrue(exception.summary.contains("User is already a data owner for company."))
    }

    private val mockAuthentication = AuthenticationMock.mockJwtAuthentication(
        "username",
        "user-id",
        setOf(DatalandRealmRole.ROLE_USER),
    )
}
