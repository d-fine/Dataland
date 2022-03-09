package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Class for defining the company data of a company
 * @param companyName identifies the data set
 * @param eutaxonomy content of the stored data
 */
data class Company(
    @field:JsonProperty("companyName", required = true) val companyName: String,
    @field:JsonProperty("eutaxonomy", required = true) val eutaxonomy: MutableList<String>
)
