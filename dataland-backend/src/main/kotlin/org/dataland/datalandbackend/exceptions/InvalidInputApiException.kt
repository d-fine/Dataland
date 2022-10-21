package org.dataland.datalandbackend.exceptions

import org.dataland.datalandbackend.model.ErrorDetails
import org.springframework.http.HttpStatus

/**
 * An InvalidInputApiException should be thrown if the provided input is somehow invalid
 */
class InvalidInputApiException(
    val summary: String,
    override val message: String,
    cause: Throwable? = null
) : SingleApiException(message, cause) {

    override fun getErrorResponse(): ErrorDetails {
        return ErrorDetails(
            errorType = "invalid-input",
            summary = summary,
            message = message,
            httpStatus = HttpStatus.BAD_REQUEST
        )
    }
}
