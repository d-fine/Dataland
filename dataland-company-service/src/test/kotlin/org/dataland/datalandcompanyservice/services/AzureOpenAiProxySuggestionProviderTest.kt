package org.dataland.datalandcompanyservice.services

import org.dataland.datalandbackendutils.exceptions.InvalidProxySuggestionApiException
import org.dataland.datalandbackendutils.exceptions.ServiceUnavailableApiException
import org.dataland.datalandcompanyservice.model.ProxySearchCompany
import org.dataland.datalandcompanyservice.model.ProxySearchLlmResult
import org.dataland.datalandcompanyservice.model.ProxySearchLlmSource
import org.dataland.datalandcompanyservice.model.ProxySearchStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

class AzureOpenAiProxySuggestionProviderTest {
    private val llmClient = mock<ProxySearchLlmClient>()
    private val company = ProxySearchCompany(UUID.randomUUID().toString(), "Child GmbH")
    private val proxyId = UUID.randomUUID().toString()
    private val provider = AzureOpenAiProxySuggestionProvider(llmClient)

    private fun stubResult(result: ProxySearchLlmResult) {
        whenever(llmClient.research(company)).thenReturn(result)
    }

    @Test
    fun `a found result is mapped to a proxy suggestion`() {
        stubResult(
            ProxySearchLlmResult(
                status = ProxySearchStatus.FOUND,
                parentName = "Parent AG",
                parentCompanyId = proxyId,
                confidence = 1.7,
                reasoning = "Annual report",
                sources = listOf(ProxySearchLlmSource("Report", "https://example.com", "official_filing")),
            ),
        )
        val suggestion = provider.suggestProxy(company)
        assertEquals(company.companyId, suggestion.companyId)
        assertEquals(proxyId, suggestion.proxyCompanyId)
        assertEquals(1.0, suggestion.confidence)
        assertEquals("Annual report", suggestion.reasoning)
        assertEquals("https://example.com", suggestion.sources.single().url)
    }

    @Test
    fun `non found statuses and missing ids result in an explained unprocessable error`() {
        val expectedExplanations =
            mapOf(
                ProxySearchLlmResult(status = ProxySearchStatus.SELF, parentCompanyId = proxyId) to "own ultimate parent",
                ProxySearchLlmResult(status = ProxySearchStatus.UNCLEAR) to "could not reliably determine",
                ProxySearchLlmResult(status = ProxySearchStatus.AMBIGUOUS, candidates = listOf("A AG", "B AG")) to
                    "candidates: A AG, B AG",
                ProxySearchLlmResult(status = ProxySearchStatus.FOUND, parentName = "Parent AG") to
                    "\"Parent AG\" as the ultimate parent",
            )
        expectedExplanations.forEach { (result, expectedText) ->
            stubResult(result.copy(reasoning = "Some reasoning"))
            val exception = assertThrows<InvalidProxySuggestionApiException> { provider.suggestProxy(company) }
            assertEquals(AzureOpenAiProxySuggestionProvider.NO_SUITABLE_PROXY_SUMMARY, exception.summary)
            assertTrue(exception.message.contains(expectedText), exception.message)
            assertTrue(exception.message.contains("Reasoning of the AI: Some reasoning"), exception.message)
        }
    }

    @Test
    fun `llm errors and unusable responses result in service unavailable`() {
        listOf(RuntimeException("429 rate limit"), ProxySearchLlmException("no answer")).forEach {
            doThrow(it).whenever(llmClient).research(company)
            assertThrows<ServiceUnavailableApiException> { provider.suggestProxy(company) }
        }
    }
}
