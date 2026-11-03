package org.dataland.datalandbackendutils.exceptions

import org.dataland.datalandbackendutils.model.ErrorDetails
import org.springframework.http.HttpStatus

/**
 * A ServiceUnavailableApiException should be thrown if a service Dataland depends on (e.g. an external LLM) is
 * temporarily not able to handle the request (error, timeout, rate limit). It is returned with a 503 status code.
 */
class ServiceUnavailableApiException(
    val summary: String,
    override val message: String,
    cause: Throwable? = null,
) : SingleApiException(message, cause) {
    override fun getErrorResponse(): ErrorDetails =
        ErrorDetails(
            errorType = "service-unavailable",
            summary = summary,
            message = message,
            httpStatus = HttpStatus.SERVICE_UNAVAILABLE,
        )
}
