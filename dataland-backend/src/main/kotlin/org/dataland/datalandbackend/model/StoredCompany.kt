package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Class for defining the fields needed to store a company in Dataland
 * @param companyName as name for the company to be stored
 * @param dataSets contains data identifiers for all data sets of this company and is initially an empty list
 */
data class StoredCompany(
    @field:JsonProperty(required = true) val companyName: String,
    @field:JsonProperty(required = true) val dataSets: MutableList<DataIdentifier>
)
