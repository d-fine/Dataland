package org.dataland.datalandbackend.exceptions

import org.dataland.datalandbackend.entities.CompanyIdentifierEntity
import org.dataland.datalandbackendutils.exceptions.SingleApiException
import org.dataland.datalandbackendutils.model.ErrorDetails
import org.springframework.http.HttpStatus

/**
 * An DuplicateIdentifierException should be thrown if a company cannot be created because of an identifier that
 * is already being used by another company.
 */
class DuplicateIdentifierException(
    val duplicateIdentifiers: List<CompanyIdentifierEntity>,
    override val message: String =
        "At least one of the identifiers you entered are already being used by another company",
) : SingleApiException(message, null) {

    override fun getErrorResponse(): ErrorDetails {
        return ErrorDetails(
            errorType = "duplicate-company-identifier",
            summary = message,
            message = message,
            httpStatus = HttpStatus.BAD_REQUEST,
            metaInformation = duplicateIdentifiers.map {
                mapOf(
                    "companyId" to it.company!!.companyId,
                    "identifierType" to it.identifierType,
                    "identifierValue" to it.identifierValue,
                )
            },
        )
    }
}
