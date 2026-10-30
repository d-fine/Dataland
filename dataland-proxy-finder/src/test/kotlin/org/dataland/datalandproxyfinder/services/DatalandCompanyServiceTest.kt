package org.dataland.datalandproxyfinder.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyId
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class DatalandCompanyServiceTest {
    private val mockCompanyDataControllerApi = mock<CompanyDataControllerApi>()
    private lateinit var datalandCompanyService: DatalandCompanyService

    private val companyId = UUID.randomUUID().toString()
    private val notFound = ClientException(statusCode = 404)
    private val forbidden = ClientException(statusCode = 403)

    @BeforeEach
    fun setup() {
        reset(mockCompanyDataControllerApi)
        datalandCompanyService = DatalandCompanyService(mockCompanyDataControllerApi)
    }

    @Test
    fun `an existing company is retrieved with its name`() {
        doReturn(CompanyInformation("Some Company", "Berlin", emptyMap(), "DE"))
            .whenever(mockCompanyDataControllerApi)
            .getCompanyInfo(companyId)

        val company = datalandCompanyService.getCompany(companyId)
        assertEquals(companyId, company.companyId)
        assertEquals("Some Company", company.companyName)
    }

    @Test
    fun `retrieving an unknown company results in a not found exception`() {
        doThrow(notFound).whenever(mockCompanyDataControllerApi).getCompanyInfo(companyId)
        assertThrows<ResourceNotFoundApiException> { datalandCompanyService.getCompany(companyId) }
    }

    @Test
    fun `retrieving a company with an invalid id results in a not found exception without calling the backend`() {
        assertThrows<ResourceNotFoundApiException> { datalandCompanyService.getCompany("not-a-uuid") }
        verify(mockCompanyDataControllerApi, never()).getCompanyInfo(any())
    }

    @Test
    fun `other client errors are not swallowed`() {
        doThrow(forbidden).whenever(mockCompanyDataControllerApi).getCompanyInfo(companyId)
        assertThrows<ClientException> { datalandCompanyService.getCompany(companyId) }
    }

    @Test
    fun `company existence is checked via the backend`() {
        assertTrue(datalandCompanyService.companyExists(companyId))
        doThrow(notFound).whenever(mockCompanyDataControllerApi).isCompanyIdValid(companyId)
        assertFalse(datalandCompanyService.companyExists(companyId))
        assertFalse(datalandCompanyService.companyExists("not-a-uuid"))
    }

    @Test
    fun `company ids are looked up by lei`() {
        doReturn(CompanyId(companyId))
            .whenever(mockCompanyDataControllerApi)
            .getCompanyIdByIdentifier(IdentifierType.Lei, "KNOWNLEI")
        doThrow(notFound).whenever(mockCompanyDataControllerApi).getCompanyIdByIdentifier(IdentifierType.Lei, "UNKNOWNLEI")

        assertEquals(companyId, datalandCompanyService.findCompanyIdByLei("KNOWNLEI"))
        assertNull(datalandCompanyService.findCompanyIdByLei("UNKNOWNLEI"))
    }
}
