package org.dataland.datalandapikeymanager.exceptions

import org.dataland.datalandapikeymanager.model.ErrorDetails
import org.springframework.http.HttpStatus

/**
 * An InternalServerErrorApiException should be thrown if something went terribly wrong on Datalands side.
 * It will be returned as a 500 error. publicSummary and publicMessage will be displayed to the user,
 * internalMessage and internalCause will only be displayed in development mode
 */
class InternalServerErrorApiException(
    val publicSummary: String,
    val publicMessage: String,
    internalMessage: String,
    internalCause: Throwable? = null
) : SingleApiException(internalMessage, internalCause) {

    constructor(internalMessage: String, internalCause: Throwable? = null) : this(
        "An internal server error occurred",
        "An unexpected internal server error occurred. Please contact support if this error persists",
        internalMessage, internalCause
    )

    override fun getErrorResponse(): ErrorDetails {
        return ErrorDetails(
            errorType = "internal-server-error",
            summary = publicSummary,
            message = publicMessage,
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}
