package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Class containing only a company's id and name
 */
data class CompanyIdAndName(
    @field:JsonProperty(required = true)
    val companyId: String,
    @field:JsonProperty(required = true)
    val companyName: String,
)
