package org.dataland.datalandbackend.model.companies

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * API-Model
 * A wrapper for the company ID
 */
data class CompanyId(
    @field:JsonProperty(required = true)
    val companyId: String,
)
