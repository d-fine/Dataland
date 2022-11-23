package org.dataland.datalandbackendutils.exceptions

import org.dataland.datalandbackendutils.model.ErrorDetails
import org.springframework.http.HttpStatus

/**
 * An InvalidInputApiException should be thrown if the provided input is somehow invalid
 */
class ApiKeyFormatException(
    override val message: String,
    cause: Throwable? = null
) : SingleApiException(message, cause) {

    override fun getErrorResponse(): ErrorDetails {
        return ErrorDetails(
            errorType = "invalid-api-key-format",
            summary = "The specified api key is of invalid format.",
            message = message,
            httpStatus = HttpStatus.BAD_REQUEST
        )
    }
}
