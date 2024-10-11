package org.dataland.datalandbackend.exceptions

import org.dataland.datalandbackend.entities.CompanyIdentifierEntity
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.model.ErrorDetails
import org.springframework.http.HttpStatus

/**
 * An DuplicateIdentifierException should be thrown if a company cannot be created because of an identifier that
 * is already being used by another company.
 */
class DuplicateIdentifierApiException(
    private val duplicateIdentifiers: List<CompanyIdentifierEntity>?,
    message: String =
        "At least one of the identifiers you entered is already being used by another company",
) : InvalidInputApiException(message, message, null) {
    override fun getErrorResponse(): ErrorDetails =
        ErrorDetails(
            errorType = "duplicate-company-identifier",
            summary = message,
            message = message,
            httpStatus = HttpStatus.BAD_REQUEST,
            metaInformation =
            duplicateIdentifiers?.map {
                mapOf(
                    "companyId" to it.company!!.companyId,
                    "identifierType" to it.identifierType,
                    "identifierValue" to it.identifierValue,
                )
            },
        )
}
