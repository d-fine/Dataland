package org.dataland.datalandproxyfinder.services

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.exceptions.ServiceUnavailableApiException
import org.dataland.datalandproxyfinder.model.ProxySearchCompany
import org.dataland.datalandproxyfinder.model.ProxySearchLlmResult
import org.dataland.datalandproxyfinder.model.ProxySearchLlmSource
import org.dataland.datalandproxyfinder.model.ProxySearchStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.ai.chat.client.ChatClient
import java.util.UUID

class AzureOpenAiProxySuggestionProviderTest {
    private val chatClient = mock<ChatClient>(defaultAnswer = org.mockito.Answers.RETURNS_DEEP_STUBS)
    private val company = ProxySearchCompany(UUID.randomUUID().toString(), "Child GmbH")
    private val proxyId = UUID.randomUUID().toString()
    private lateinit var provider: AzureOpenAiProxySuggestionProvider

    @BeforeEach
    fun setup() {
        provider = AzureOpenAiProxySuggestionProvider(chatClient, mock<DatalandCompanyService>())
    }

    private fun llmCall() =
        chatClient
            .prompt()
            .user(any<String>())
            .tools(any<Any>())
            .call()
            .entity(ProxySearchLlmResult::class.java)

    private fun stubEntity(result: ProxySearchLlmResult?) {
        whenever(llmCall()).thenReturn(result)
    }

    @Test
    fun `a found result is mapped to a proxy suggestion`() {
        stubEntity(
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
    fun `non found statuses and missing ids result in not found`() {
        listOf(ProxySearchStatus.SELF, ProxySearchStatus.UNCLEAR, ProxySearchStatus.AMBIGUOUS).forEach {
            stubEntity(ProxySearchLlmResult(status = it, parentCompanyId = proxyId))
            assertThrows<ResourceNotFoundApiException> { provider.suggestProxy(company) }
        }
        stubEntity(ProxySearchLlmResult(status = ProxySearchStatus.FOUND, parentCompanyId = null))
        assertThrows<ResourceNotFoundApiException> { provider.suggestProxy(company) }
    }

    @Test
    fun `llm errors and empty responses result in service unavailable`() {
        whenever(llmCall()).thenThrow(RuntimeException("429 rate limit"))
        assertThrows<ServiceUnavailableApiException> { provider.suggestProxy(company) }
        stubEntity(null)
        assertThrows<ServiceUnavailableApiException> { provider.suggestProxy(company) }
    }
}
