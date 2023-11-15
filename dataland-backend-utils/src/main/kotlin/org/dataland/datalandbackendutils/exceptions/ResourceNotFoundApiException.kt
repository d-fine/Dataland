package org.dataland.datalandbackendutils.exceptions

import org.dataland.datalandbackendutils.model.ErrorDetails
import org.springframework.http.HttpStatus

/**
 * A ResourceNotFoundApiException should be thrown if a dataland-internal resource could not be located
 * Both message and summary are displayed to the user with a 404 status code
 */
class ResourceNotFoundApiException(
    val summary: String,
    override val message: String,
    cause: Throwable? = null,
) : SingleApiException(message, cause) {
    override fun getErrorResponse(): ErrorDetails {
        return ErrorDetails(
            errorType = "resource-not-found",
            summary = summary,
            message = message,
            httpStatus = HttpStatus.NOT_FOUND,
        )
    }
}
