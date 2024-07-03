package org.dataland.datalandbackendutils.exceptions

import org.dataland.datalandbackendutils.model.ErrorDetails
import org.springframework.http.HttpStatus

/**
 * An InvalidPatchApiException should be thrown if the requester does not have the required rights
 * Both message and summary are displayed to the user with a 403 status code
 */
open class InvalidPatchApiException(
    val summary: String,
    override val message: String,
    cause: Throwable? = null,
) : SingleApiException(message, cause) {
    override fun getErrorResponse(): ErrorDetails {
        return ErrorDetails(
            errorType = "access-denied",
            summary = summary,
            message = message,
            httpStatus = HttpStatus.FORBIDDEN,
        )
    }
}
