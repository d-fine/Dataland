package org.dataland.datalandproxyfinder.services

import org.dataland.datalandbackendutils.exceptions.InvalidProxySuggestionApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.proxyfinder.ProxySuggestion
import org.dataland.datalandproxyfinder.model.ProxySearchCompany
import org.junit.jupiter.api.Assertions.assertEquals
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

class ProxyFinderServiceTest {
    private val mockDatalandCompanyService = mock<DatalandCompanyService>()
    private val mockProvider = mock<ProxySuggestionProvider>()
    private val mockValidator = mock<ProxySuggestionValidator>()

    private lateinit var proxyFinderService: ProxyFinderService

    private val company = ProxySearchCompany(UUID.randomUUID().toString(), "Some Company")
    private val suggestion =
        ProxySuggestion(
            companyId = company.companyId,
            proxyCompanyId = UUID.randomUUID().toString(),
            confidence = 0.5,
            reasoning = "reasoning",
            sources = emptyList(),
        )

    @BeforeEach
    fun setup() {
        reset(mockDatalandCompanyService, mockProvider, mockValidator)
        proxyFinderService = ProxyFinderService(mockDatalandCompanyService, mockProvider, mockValidator)
    }

    @Test
    fun `a validated suggestion of the provider is returned`() {
        doReturn(company).whenever(mockDatalandCompanyService).getCompany(company.companyId)
        doReturn(suggestion).whenever(mockProvider).suggestProxy(company)

        assertEquals(suggestion, proxyFinderService.findProxy(company.companyId))
        verify(mockValidator).validate(company.companyId, suggestion)
    }

    @Test
    fun `no suggestion is requested for an unknown company`() {
        doThrow(ResourceNotFoundApiException("summary", "message"))
            .whenever(mockDatalandCompanyService)
            .getCompany(company.companyId)

        assertThrows<ResourceNotFoundApiException> { proxyFinderService.findProxy(company.companyId) }
        verify(mockProvider, never()).suggestProxy(any())
    }

    @Test
    fun `an invalid suggestion is not returned`() {
        doReturn(company).whenever(mockDatalandCompanyService).getCompany(company.companyId)
        doReturn(suggestion).whenever(mockProvider).suggestProxy(company)
        doThrow(InvalidProxySuggestionApiException("summary", "message"))
            .whenever(mockValidator)
            .validate(company.companyId, suggestion)

        assertThrows<InvalidProxySuggestionApiException> { proxyFinderService.findProxy(company.companyId) }
    }
}
