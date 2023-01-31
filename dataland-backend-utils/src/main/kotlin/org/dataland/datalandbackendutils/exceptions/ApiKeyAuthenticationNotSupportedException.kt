package org.dataland.datalandbackendutils.exceptions

import org.dataland.datalandbackendutils.model.ErrorDetails
import org.springframework.http.HttpStatus

/**
 * An Exception thrown when API-Key authentication is used to access is resource that is not designed for API-Key
 * authentication.
 */
class ApiKeyAuthenticationNotSupportedException(
    val summary: String = "API-Key Authentication is not supported for this request",
    override val message: String =
        "API-Key Authentication is not supported for this request, please authenticate in a different way.",
    cause: Throwable? = null
) : SingleApiException(message, cause) {

    override fun getErrorResponse(): ErrorDetails {
        return ErrorDetails(
            errorType = "api-key-authentication-not-supported",
            summary = summary,
            message = message,
            httpStatus = HttpStatus.FORBIDDEN
        )
    }
}
