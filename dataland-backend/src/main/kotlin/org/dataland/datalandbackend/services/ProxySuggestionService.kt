package org.dataland.datalandbackend.services

import com.fasterxml.jackson.core.JacksonException
import org.dataland.datalandbackendutils.exceptions.COMPANY_NOT_FOUND
import org.dataland.datalandbackendutils.exceptions.InvalidProxySuggestionApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.proxyfinder.ProxySuggestion
import org.dataland.datalandbackendutils.model.proxyfinder.ProxySuggestionSource
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandproxyfinder.openApiClient.api.ProxySuggestionControllerApi
import org.dataland.datalandproxyfinder.openApiClient.infrastructure.ClientError
import org.dataland.datalandproxyfinder.openApiClient.infrastructure.ClientException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.dataland.datalandproxyfinder.openApiClient.model.ProxySuggestion as ProxyFinderProxySuggestion

/**
 * Requests proxy suggestions from the proxy finder and translates its errors into Dataland API errors.
 */
@Service
class ProxySuggestionService(
    private val proxySuggestionControllerApi: ProxySuggestionControllerApi,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Requests a proxy suggestion for the given company from the proxy finder.
     * @param companyId the Dataland company ID of the company for which a proxy is searched
     * @return the validated proxy suggestion of the proxy finder
     * @throws ResourceNotFoundApiException if the company does not exist on Dataland
     * @throws InvalidProxySuggestionApiException if the proxy finder could not produce a valid suggestion
     */
    fun requestProxySuggestion(companyId: String): ProxySuggestion {
        logger.info("Requesting proxy suggestion for company $companyId from the proxy finder.")
        val suggestion =
            try {
                proxySuggestionControllerApi.findProxySuggestion(companyId)
            } catch (clientException: ClientException) {
                throw translateClientException(companyId, clientException)
            }
        return suggestion.toProxySuggestion()
    }

    private fun translateClientException(
        companyId: String,
        clientException: ClientException,
    ): Exception =
        when (clientException.statusCode) {
            HttpStatus.NOT_FOUND.value() ->
                ResourceNotFoundApiException(
                    summary = COMPANY_NOT_FOUND,
                    message = "Dataland does not know the company ID $companyId.",
                    cause = clientException,
                )

            HttpStatus.UNPROCESSABLE_CONTENT.value() -> {
                val (summary, message) = extractErrorSummaryAndMessage(clientException)
                InvalidProxySuggestionApiException(
                    summary = summary ?: "Invalid proxy suggestion.",
                    message = message ?: "The proxy finder could not produce a valid proxy suggestion for company $companyId.",
                    cause = clientException,
                )
            }

            else -> clientException
        }

    private fun extractErrorSummaryAndMessage(clientException: ClientException): Pair<String?, String?> {
        val body = (clientException.response as? ClientError<*>)?.body as? String ?: return Pair(null, null)
        return try {
            val firstError = defaultObjectMapper.readTree(body).path("errors").path(0)
            Pair(
                firstError.path("summary").textValue(),
                firstError.path("message").textValue(),
            )
        } catch (exception: JacksonException) {
            logger.warn("Could not parse error response of the proxy finder: ${exception.message}")
            Pair(null, null)
        }
    }

    private fun ProxyFinderProxySuggestion.toProxySuggestion() =
        ProxySuggestion(
            companyId = companyId,
            proxyCompanyId = proxyCompanyId,
            confidence = confidence,
            reasoning = reasoning,
            sources = sources.map { ProxySuggestionSource(title = it.title, url = it.url) },
        )
}
