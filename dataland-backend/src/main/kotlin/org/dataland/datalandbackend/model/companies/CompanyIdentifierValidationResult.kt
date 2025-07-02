package org.dataland.datalandbackend.model.companies

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.entities.BasicCompanyInformation
import org.dataland.datalandbackendutils.utils.BackendOpenApiDescriptionsAndExamples

/**
 * API-Model
 * The result of a company identifier validation
 */
data class CompanyIdentifierValidationResult(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.SINGLE_IDENTIFIER_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.SINGLE_IDENTIFIER_EXAMPLE,
    )
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
