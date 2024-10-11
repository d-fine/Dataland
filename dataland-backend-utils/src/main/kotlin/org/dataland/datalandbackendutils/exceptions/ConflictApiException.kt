package org.dataland.datalandbackendutils.exceptions

import org.dataland.datalandbackendutils.model.ErrorDetails
import org.springframework.http.HttpStatus

/**
 * A ConflictApiException should be thrown if there appears a conflict in processing the underlying Api request
 */
open class ConflictApiException(
    val summary: String,
    override val message: String,
    cause: Throwable? = null,
) : SingleApiException(message, cause) {
    override fun getErrorResponse(): ErrorDetails =
        ErrorDetails(
            errorType = "conflict",
            summary = summary,
            message = message,
            httpStatus = HttpStatus.CONFLICT,
        )
}
