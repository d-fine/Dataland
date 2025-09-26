package org.dataland.datasourcingservice.exceptions

import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.model.ErrorDetails
import org.springframework.http.HttpStatus
import java.util.UUID

/**
 * Exception thrown when there is an attempt to create a duplicate request
 * for the same company, framework, and reporting period while an existing
 * request is still in a non-final state.
 */
class DuplicateRequestException(
    val id: UUID,
    val reportingPeriod: String,
    val companyId: UUID,
    val dataType: String,
    message: String = "There already exists a request in a non-final state for this company, framework and reporting period.",
) : InvalidInputApiException(message, message, null) {
    override fun getErrorResponse(): ErrorDetails =
        ErrorDetails(
            errorType = "duplicate-request",
            summary = message,
            message = message,
            httpStatus = HttpStatus.BAD_REQUEST,
            metaInformation =
                mapOf(
                    "id" to id.toString(),
                    "reportingPeriod" to reportingPeriod,
                    "companyId" to companyId.toString(),
                    "framework" to dataType,
                ),
        )
}
