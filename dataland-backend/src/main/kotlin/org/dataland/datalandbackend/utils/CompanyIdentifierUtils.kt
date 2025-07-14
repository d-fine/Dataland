package org.dataland.datalandbackend.utils

import org.dataland.datalandbackend.model.enums.company.IdentifierType

object CompanyIdentifierUtils {
    const val COMPANY_NOT_FOUND_SUMMARY = "Company identifier does not exist"

    fun companyNotFoundMessage(
        identifierType: IdentifierType,
        identifier: String,
    ) = "Company identifier $identifier of type $identifierType does not exist"
}
