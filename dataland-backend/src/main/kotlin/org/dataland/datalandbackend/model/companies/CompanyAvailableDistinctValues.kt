package org.dataland.datalandbackend.model.companies

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Class that returns all available countryCodes and sectors
 * @param countryCodes List of available countryCodes
 * @param sectors List of available sectors
 */
data class CompanyAvailableDistinctValues(
    @field:JsonProperty(required = true)
    val countryCodes: Set<String>,
    @field:JsonProperty(required = true)
    val sectors: Set<String>,
)
