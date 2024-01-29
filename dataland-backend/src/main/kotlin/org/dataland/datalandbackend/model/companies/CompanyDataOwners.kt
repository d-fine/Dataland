package org.dataland.datalandbackend.model.companies

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- Generic API model ---
 * DTO for uploading information about company data ownership relations
 * @param companyId identifier of the company the ownership belong(s) to
 * @param dataOwners list of IDs corresponding to users that own data corresponding to the company
 */
data class CompanyDataOwners(
    @field:JsonProperty(required = true)
    val companyId: String,

    @field:JsonProperty(required = true)
    val dataOwners: List<String>,
)
