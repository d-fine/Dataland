package org.dataland.datalandbackendutils.exceptions

import org.dataland.datalandbackendutils.model.ErrorDetails
import org.springframework.http.HttpStatus

/**
 * A quota exceeded Excpetion should be thrown if an internal quota is exceeded
 */
open class QuotaExceededException(
    val summary: String,
    override val message: String,
    cause: Throwable? = null,
) : SingleApiException(message, cause) {
    override fun getErrorResponse(): ErrorDetails =
        ErrorDetails(
            errorType = "quota-exceeded",
            summary = summary,
            message = message,
            httpStatus = HttpStatus.FORBIDDEN,
        )
}
