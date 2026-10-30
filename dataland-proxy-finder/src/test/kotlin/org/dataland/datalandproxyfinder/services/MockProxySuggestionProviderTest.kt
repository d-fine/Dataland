package org.dataland.datalandproxyfinder.services

import org.dataland.datalandbackendutils.exceptions.InvalidProxySuggestionApiException
import org.dataland.datalandproxyfinder.model.ProxySearchCompany
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import java.util.UUID

class MockProxySuggestionProviderTest {
    private val mockDatalandCompanyService = mock<DatalandCompanyService>()
    private val proxyCompanyLei = "DALAPROXYFINDERMOCK1"
    private val proxyCompanyId = UUID.randomUUID().toString()

    private lateinit var provider: MockProxySuggestionProvider

    @BeforeEach
    fun setup() {
        reset(mockDatalandCompanyService)
        provider = MockProxySuggestionProvider(mockDatalandCompanyService, proxyCompanyLei)
    }

    @Test
    fun `the same hardcoded suggestion is returned for any input company`() {
        doReturn(proxyCompanyId).whenever(mockDatalandCompanyService).findCompanyIdByLei(proxyCompanyLei)
        val firstCompany = ProxySearchCompany(UUID.randomUUID().toString(), "First Company AG")
        val secondCompany = ProxySearchCompany(UUID.randomUUID().toString(), "Second Company GmbH")

        val firstSuggestion = provider.suggestProxy(firstCompany)
        val secondSuggestion = provider.suggestProxy(secondCompany)

        assertEquals(firstCompany.companyId, firstSuggestion.companyId)
        assertEquals(secondCompany.companyId, secondSuggestion.companyId)
        listOf(firstSuggestion, secondSuggestion).forEach {
            assertEquals(proxyCompanyId, it.proxyCompanyId)
            assertEquals(MockProxySuggestionProvider.MOCK_CONFIDENCE, it.confidence)
            assertEquals(MockProxySuggestionProvider.MOCK_REASONING, it.reasoning)
            assertEquals(MockProxySuggestionProvider.MOCK_SOURCES, it.sources)
        }
    }

    @Test
    fun `an exception is thrown if the configured proxy company does not exist`() {
        doReturn(null).whenever(mockDatalandCompanyService).findCompanyIdByLei(proxyCompanyLei)

        val exception =
            assertThrows<InvalidProxySuggestionApiException> {
                provider.suggestProxy(ProxySearchCompany(UUID.randomUUID().toString(), "Some Company"))
            }
        assertEquals(ProxySuggestionValidator.UNKNOWN_PROXY_COMPANY_SUMMARY, exception.summary)
    }
}
