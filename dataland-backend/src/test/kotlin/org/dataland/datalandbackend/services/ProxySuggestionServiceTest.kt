package org.dataland.datalandbackend.services

import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.dataland.datalandbackendutils.exceptions.InvalidProxySuggestionApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandproxyfinder.openApiClient.api.ProxySuggestionControllerApi
import org.dataland.datalandproxyfinder.openApiClient.infrastructure.ClientError
import org.dataland.datalandproxyfinder.openApiClient.infrastructure.ClientException
import org.dataland.datalandproxyfinder.openApiClient.infrastructure.ServerError
import org.dataland.datalandproxyfinder.openApiClient.infrastructure.ServerException
import org.dataland.datalandproxyfinder.openApiClient.model.ProxySuggestion
import org.dataland.datalandproxyfinder.openApiClient.model.ProxySuggestionSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import java.net.SocketTimeoutException
import java.util.UUID

class ProxySuggestionServiceTest {
    private val mockProxySuggestionControllerApi = mock<ProxySuggestionControllerApi>()
    private lateinit var proxySuggestionService: ProxySuggestionService

    private val companyId = UUID.randomUUID().toString()

    @BeforeEach
    fun setup() {
        reset(mockProxySuggestionControllerApi)
        proxySuggestionService = ProxySuggestionService(mockProxySuggestionControllerApi)
    }

    private fun clientException(
        statusCode: Int,
        body: String? = null,
    ) = ClientException("Client error", statusCode, ClientError<Any>(body = body, statusCode = statusCode))

    @Test
    fun `the suggestion of the proxy finder is mapped to the shared model`() {
        val proxyCompanyId = UUID.randomUUID().toString()
        doReturn(
            ProxySuggestion(companyId, proxyCompanyId, 0.7, "reasoning", listOf(ProxySuggestionSource("title", "https://x.y"))),
        ).whenever(mockProxySuggestionControllerApi).findProxySuggestion(companyId)

        val suggestion = proxySuggestionService.requestProxySuggestion(companyId)

        assertEquals(companyId, suggestion.companyId)
        assertEquals(proxyCompanyId, suggestion.proxyCompanyId)
        assertEquals(0.7, suggestion.confidence)
        assertEquals("reasoning", suggestion.reasoning)
        assertEquals("title", suggestion.sources.single().title)
        assertEquals("https://x.y", suggestion.sources.single().url)
    }

    @Test
    fun `a 404 of the proxy finder is translated into a resource not found exception`() {
        doThrow(clientException(404)).whenever(mockProxySuggestionControllerApi).findProxySuggestion(companyId)
        assertThrows<ResourceNotFoundApiException> { proxySuggestionService.requestProxySuggestion(companyId) }
    }

    @Test
    fun `a 422 of the proxy finder is translated into an invalid proxy suggestion exception with its details`() {
        val body = """{"errors":[{"summary":"Some summary","message":"Some message"}]}"""
        doThrow(clientException(422, body)).whenever(mockProxySuggestionControllerApi).findProxySuggestion(companyId)

        val exception =
            assertThrows<InvalidProxySuggestionApiException> { proxySuggestionService.requestProxySuggestion(companyId) }
        assertEquals("Some summary", exception.summary)
        assertEquals("Some message", exception.message)
    }

    @Test
    fun `a 422 of the proxy finder with an unreadable body results in a generic invalid proxy suggestion exception`() {
        doThrow(clientException(422, "not json")).whenever(mockProxySuggestionControllerApi).findProxySuggestion(companyId)

        val exception =
            assertThrows<InvalidProxySuggestionApiException> { proxySuggestionService.requestProxySuggestion(companyId) }
        assertEquals("Invalid proxy suggestion.", exception.summary)
    }

    @Test
    fun `other client errors of the proxy finder are translated into an internal server error`() {
        val forbidden = clientException(403, """{"errors":[]}""")
        doThrow(forbidden).whenever(mockProxySuggestionControllerApi).findProxySuggestion(companyId)

        val exception =
            assertThrows<InternalServerErrorApiException> { proxySuggestionService.requestProxySuggestion(companyId) }
        assertEquals(PROXY_FINDER_UNAVAILABLE_SUMMARY, exception.publicSummary)
        assertSame(forbidden, exception.cause)
    }

    @Test
    fun `server errors of the proxy finder are translated into an internal server error`() {
        val serverException =
            ServerException("Server error", 503, ServerError<Any>(body = "unavailable", statusCode = 503))
        doThrow(serverException).whenever(mockProxySuggestionControllerApi).findProxySuggestion(companyId)

        val exception =
            assertThrows<InternalServerErrorApiException> { proxySuggestionService.requestProxySuggestion(companyId) }
        assertEquals(PROXY_FINDER_UNAVAILABLE_SUMMARY, exception.publicSummary)
        assertSame(serverException, exception.cause)
    }

    @Test
    fun `a timeout when calling the proxy finder is translated into an internal server error`() {
        val timeout = SocketTimeoutException("timeout")
        doThrow(timeout).whenever(mockProxySuggestionControllerApi).findProxySuggestion(companyId)

        val exception =
            assertThrows<InternalServerErrorApiException> { proxySuggestionService.requestProxySuggestion(companyId) }
        assertEquals(PROXY_FINDER_UNAVAILABLE_SUMMARY, exception.publicSummary)
        assertSame(timeout, exception.cause)
    }
}
