package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Class for defining the meta data of a company
 * @param companyName name of the company
 * @param countryCode identifies the country
 */
data class Skyminder(
    @field:JsonProperty(required = true) val companyName: String,
    @field:JsonProperty(required = true) val countryCode: String
)
