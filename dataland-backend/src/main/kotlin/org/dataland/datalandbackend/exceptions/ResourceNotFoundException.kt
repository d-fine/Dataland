package org.dataland.datalandbackend.exceptions

import org.dataland.datalandbackend.model.ErrorDetails
import org.springframework.http.HttpStatus

/**
 * A ResourceNotFoundException should be thrown if a dataland-internal resource could not be located
 * Both message and summary are displayed to the user with a 404 status code
 */
class ResourceNotFoundException(
    val summary: String,
    override val message: String,
    cause: Throwable?
) : SingleApiException(message, cause) {

    constructor(summary: String, message: String) : this(summary, message, null)

    override fun getErrorResponse(): ErrorDetails {
        return ErrorDetails(
            errorCode = "resource-not-found",
            summary = summary,
            message = message,
            httpStatus = HttpStatus.NOT_FOUND
        )
    }
}
