package org.dataland.datalandbackendutils.exceptions

import org.dataland.datalandbackendutils.model.ErrorDetails
import org.springframework.http.HttpStatus

const val INVALID_PROXY_SUGGESTION_ERROR_TYPE = "invalid-proxy-suggestion"

/**
 * An InvalidProxySuggestionApiException should be thrown if the proxy finder produced a suggestion that violates the
 * validation rules for proxy suggestions (e.g., the suggested company does not exist on Dataland or is the company
 * itself). It is returned with a 422 status code.
 */
class InvalidProxySuggestionApiException(
    val summary: String,
    override val message: String,
    cause: Throwable? = null,
) : SingleApiException(message, cause) {
    override fun getErrorResponse(): ErrorDetails =
        ErrorDetails(
            errorType = INVALID_PROXY_SUGGESTION_ERROR_TYPE,
            summary = summary,
            message = message,
            httpStatus = HttpStatus.UNPROCESSABLE_CONTENT,
        )
}
