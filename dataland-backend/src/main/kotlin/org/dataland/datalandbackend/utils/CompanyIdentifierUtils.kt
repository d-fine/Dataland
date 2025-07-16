package org.dataland.datalandbackend.utils

import org.dataland.datalandbackend.model.enums.company.IdentifierType

object CompanyIdentifierUtils {
    const val COMPANY_NOT_FOUND_SUMMARY = "Company identifier does not exist"

    /**
     * Generates a message indicating that a company identifier of a specific type does not exist.
     *
     * @param identifierType The type of the identifier (e.g., LEI, ISIN).
     * @param identifier The value of the identifier that was not found.
     * @return A formatted message indicating the absence of the company identifier.
     */
    fun companyNotFoundMessage(
        identifierType: IdentifierType,
        identifier: String,
    ) = "Company identifier $identifier of type $identifierType does not exist"
}
