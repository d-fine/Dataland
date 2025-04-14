package org.dataland.datalandbackend.model.companies

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.entities.BasicCompanyInformation

/**
 * API-Model
 * The result of a company identifier validation
 */
data class CompanyIdentifierValidationResult(
    @field:JsonProperty(required = true)
    val identifier: String,
    val companyInformation: BasicCompanyInformation? = null,
) {
    constructor(
        identifier: String,
        companyId: String,
        companyName: String,
        headquarters: String,
        countryCode: String,
        sector: String? = null,
        lei: String? = null,
    ) : this(
        identifier,
        companyInformation =
            BasicCompanyInformation(
                companyId = companyId,
                companyName = companyName,
                headquarters = headquarters,
                countryCode = countryCode,
                sector = sector,
                lei = lei,
            ),
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CompanyIdentifierValidationResult) return false

        return identifier == other.identifier &&
            companyInformation == other.companyInformation
    }

    override fun hashCode(): Int = identifier.hashCode() * 31 + (companyInformation?.hashCode() ?: 0)
}
