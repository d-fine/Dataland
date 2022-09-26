package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Class for defining the request body of a post company request
 * @param companyName name of the company
 * @param headquarters city where the headquarters of the company is located
 * @param sector in which the company operates
 */
data class CompanyInformation(
    @field:JsonProperty(required = true)
    val companyName: String,

    @field:JsonProperty(required = true)
    val headquarters: String,

    @field:JsonProperty(required = true)
    val sector: String,

    @field:JsonProperty(required = true)
    val identifiers: List<CompanyIdentifier>,

    @field:JsonProperty(required = true)
    val countryCode: String,

    @get:JsonProperty(value = "isTeaserCompany")
    val isTeaserCompany: Boolean = false,
)
