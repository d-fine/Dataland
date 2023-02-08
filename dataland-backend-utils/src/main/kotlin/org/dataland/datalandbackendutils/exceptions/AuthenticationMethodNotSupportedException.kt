package org.dataland.datalandbackendutils.exceptions

import org.dataland.datalandbackendutils.model.ErrorDetails
import org.springframework.http.HttpStatus

/**
 * An Exception thrown when an authentication method is used to access a resource that is not designed for that kind
 * of authentication
 */
class AuthenticationMethodNotSupportedException(
    val summary: String = "The chosen authentication method is not supported for this request",
    override val message: String =
        "The chosen authentication method is not supported for this request, please authenticate in a different way.",
    cause: Throwable? = null
) : SingleApiException(message, cause) {

    override fun getErrorResponse(): ErrorDetails {
        return ErrorDetails(
            errorType = "authentication-method-not-supported-for-this-request",
            summary = summary,
            message = message,
            httpStatus = HttpStatus.FORBIDDEN
        )
    }
}
