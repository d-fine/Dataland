package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Class for defining the meta data of a company
 * @param companyName name of the company
 * @param companyId identifies the company
 */
data class CompanyMetaInformation(
    @field:JsonProperty(required = true) val companyName: String,
    @field:JsonProperty(required = true) val companyId: String
)
