package org.dataland.datalandbackendutils.exceptions

import org.dataland.datalandbackendutils.model.ErrorDetails
import org.springframework.http.HttpStatus

/**
 * An InsufficientRightsApiException should be thrown if the user rights are somehow not sufficient to perform a request
 */
open class InsufficientRightsApiException(
    val summary: String,
    override val message: String,
    cause: Throwable? = null,
) : SingleApiException(message, cause) {
    override fun getErrorResponse(): ErrorDetails =
        ErrorDetails(
            errorType = "insufficient-rights",
            summary = summary,
            message = message,
            httpStatus = HttpStatus.FORBIDDEN,
        )
}
