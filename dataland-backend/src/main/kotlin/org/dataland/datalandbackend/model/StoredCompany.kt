package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Class for defining the company data of a company
 * @param companyName identifies the data set
 * @param dataSets content of the stored data
 */
data class StoredCompany(
    @field:JsonProperty("companyName", required = true) val companyName: String,
    @field:JsonProperty("dataSets", required = true) val dataSets: MutableList<DataIdentifier>
)
