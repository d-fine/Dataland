package org.dataland.datalandbackend.model.companies

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * API-Model
 * The result of a company identifier validation
 */
data class CompanyIdentifierValidationResult(
    @field:JsonProperty(required = true)
    val identifier: String,
    val companyId: String? = null,
    val companyName: String? = null,
    val sector: String? = null,
    val countryCode: String? = null,
)
