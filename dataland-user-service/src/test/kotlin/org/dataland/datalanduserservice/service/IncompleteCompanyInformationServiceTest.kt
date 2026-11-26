package org.dataland.datalanduserservice.service

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.infrastructure.ServerException
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalanduserservice.entity.IncompleteCompanyInformationEntity
import org.dataland.datalanduserservice.repository.IncompleteCompanyInformationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus

class IncompleteCompanyInformationServiceTest {
    private val repository = mock<IncompleteCompanyInformationRepository>()
    private val companyDataControllerApi = mock<CompanyDataControllerApi>()
    private val service = IncompleteCompanyInformationService(repository, companyDataControllerApi)
    private val correlationId = "correlation-id"

    @BeforeEach
    fun setup() {
        reset(repository, companyDataControllerApi)
    }

    private fun stubCompany(
        id: String,
        fiscalYearEnd: String?,
        sector: String?,
    ) {
        whenever(companyDataControllerApi.getCompanyById(id)).thenReturn(
            StoredCompany(
                companyId = id,
                companyInformation =
                    CompanyInformation(
                        companyName = "Name $id",
                        headquarters = "HQ",
                        identifiers = emptyMap(),
                        countryCode = "DE",
                        fiscalYearEnd = fiscalYearEnd,
                        sector = sector,
                    ),
                dataRegisteredByDataland = emptyList(),
            ),
        )
    }

    private fun stubEntries(vararg ids: String) {
        whenever(repository.findAll()).thenReturn(ids.map { IncompleteCompanyInformationEntity(it) })
    }

    @Test
    fun `registering a new company saves an entry`() {
        stubCompany("c1", null, null)
        whenever(repository.existsById("c1")).thenReturn(false)
        whenever(repository.saveAndFlush(any<IncompleteCompanyInformationEntity>()))
            .thenAnswer { it.getArgument<IncompleteCompanyInformationEntity>(0) }
        service.registerIncompleteCompany("c1", correlationId)
        verify(repository).saveAndFlush(any<IncompleteCompanyInformationEntity>())
    }

    @Test
    fun `registering an already logged company does nothing`() {
        stubCompany("c1", null, null)
        whenever(repository.existsById("c1")).thenReturn(true)
        service.registerIncompleteCompany("c1", correlationId)
        verify(repository, never()).saveAndFlush(any<IncompleteCompanyInformationEntity>())
    }

    @Test
    fun `registering swallows a concurrent insert of the same company`() {
        stubCompany("c1", null, null)
        whenever(repository.existsById("c1")).thenReturn(false)
        whenever(repository.saveAndFlush(any<IncompleteCompanyInformationEntity>()))
            .thenThrow(DataIntegrityViolationException("duplicate"))
        service.registerIncompleteCompany("c1", correlationId)
    }

    @Test
    fun `registering an unknown company results in not found`() {
        whenever(companyDataControllerApi.getCompanyById("c1"))
            .thenThrow(ClientException(statusCode = HttpStatus.NOT_FOUND.value()))
        assertThrows<ResourceNotFoundApiException> { service.registerIncompleteCompany("c1", correlationId) }
        verify(repository, never()).saveAndFlush(any<IncompleteCompanyInformationEntity>())
    }

    @Test
    fun `registering propagates other client errors`() {
        whenever(companyDataControllerApi.getCompanyById("c1"))
            .thenThrow(ClientException(statusCode = HttpStatus.BAD_REQUEST.value()))
        assertThrows<ClientException> { service.registerIncompleteCompany("c1", correlationId) }
    }

    @Test
    fun `getting the log keeps incomplete and removes complete companies`() {
        stubEntries("missingFye", "missingSector", "blankSector", "complete")
        stubCompany("missingFye", null, "Energy")
        stubCompany("missingSector", "31-Mar", null)
        stubCompany("blankSector", "31-Mar", " ")
        stubCompany("complete", "31-Mar", "Energy")
        assertEquals(setOf("missingFye", "missingSector", "blankSector"), service.getIncompleteCompanyIds())
        verify(repository).deleteById("complete")
        verify(repository, never()).deleteById("missingFye")
        verify(repository, never()).deleteById("missingSector")
        verify(repository, never()).deleteById("blankSector")
    }

    @Test
    fun `getting the log removes entries of companies the backend no longer knows`() {
        stubEntries("gone")
        whenever(companyDataControllerApi.getCompanyById("gone"))
            .thenThrow(ClientException(statusCode = HttpStatus.NOT_FOUND.value()))
        assertEquals(emptySet<String>(), service.getIncompleteCompanyIds())
        verify(repository).deleteById("gone")
    }

    @Test
    fun `getting the log keeps entries when the backend fails`() {
        stubEntries("c1", "c2", "c3")
        whenever(companyDataControllerApi.getCompanyById("c1")).thenThrow(ServerException(statusCode = 500))
        whenever(companyDataControllerApi.getCompanyById("c2"))
            .thenThrow(ClientException(statusCode = HttpStatus.BAD_REQUEST.value()))
        whenever(companyDataControllerApi.getCompanyById("c3")).thenThrow(java.io.IOException("network"))
        assertEquals(setOf("c1", "c2", "c3"), service.getIncompleteCompanyIds())
        verify(repository, never()).deleteById(any())
    }
}
