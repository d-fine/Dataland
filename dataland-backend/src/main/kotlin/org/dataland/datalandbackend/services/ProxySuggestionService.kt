package org.dataland.datalandbackend.services

import com.fasterxml.jackson.core.JacksonException
import org.dataland.datalandbackendutils.exceptions.COMPANY_NOT_FOUND
import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.dataland.datalandbackendutils.exceptions.InvalidProxySuggestionApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.proxyfinder.ProxySuggestion
import org.dataland.datalandbackendutils.model.proxyfinder.ProxySuggestionSource
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandcompanyservice.openApiClient.api.ProxySuggestionControllerApi
import org.dataland.datalandcompanyservice.openApiClient.infrastructure.ClientError
import org.dataland.datalandcompanyservice.openApiClient.infrastructure.ClientException
import org.dataland.datalandcompanyservice.openApiClient.infrastructure.ServerError
import org.dataland.datalandcompanyservice.openApiClient.infrastructure.ServerException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.io.IOException
import org.dataland.datalandcompanyservice.openApiClient.model.ProxySuggestion as ProxyFinderProxySuggestion

const val COMPANY_SERVICE_UNAVAILABLE_SUMMARY = "Proxy finder unavailable"

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
     * @throws InternalServerErrorApiException if the proxy finder is unavailable or failed for a server-side reason
     */
    fun requestProxySuggestion(companyId: String): ProxySuggestion {
        logger.info("Requesting proxy suggestion for company $companyId from the proxy finder.")
        return runCatching { proxySuggestionControllerApi.findProxySuggestion(companyId) }
            .getOrElse { throw translateProxyFinderException(companyId, it) }
            .toProxySuggestion()
    }

    /**
     * Translates exceptions of the proxy finder client into Dataland API exceptions. Server errors and connection
     * failures (including timeouts) are server-side problems the caller cannot fix, so they become internal server
     * errors. Unexpected throwables are passed on unchanged.
     */
    private fun translateProxyFinderException(
        companyId: String,
        throwable: Throwable,
    ): Throwable =
        when (throwable) {
            is ClientException -> translateClientException(companyId, throwable)

            is ServerException -> {
                logger.error(
                    "The proxy finder returned status ${throwable.statusCode} for company $companyId. " +
                        "Response body: ${(throwable.response as? ServerError<*>)?.body}",
                )
                proxyFinderUnavailable(companyId, throwable)
            }

            is IOException -> {
                logger.error("Could not reach the proxy finder for company $companyId: ${throwable.message}")
                proxyFinderUnavailable(companyId, throwable)
            }

            else -> throwable
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

            else -> {
                logger.error(
                    "The proxy finder returned status ${clientException.statusCode} for company $companyId. " +
                        "Response body: ${(clientException.response as? ClientError<*>)?.body}",
                )
                proxyFinderUnavailable(companyId, clientException)
            }
        }

    private fun proxyFinderUnavailable(
        companyId: String,
        cause: Exception,
    ) = InternalServerErrorApiException(
        publicSummary = COMPANY_SERVICE_UNAVAILABLE_SUMMARY,
        publicMessage = "The proxy finder could not process the request. Please try again later or contact support.",
        internalMessage = "Requesting a proxy suggestion for company $companyId from the proxy finder failed: ${cause.message}",
        internalCause = cause,
    )

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
