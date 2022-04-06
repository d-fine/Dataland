package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Class for defining the meta data of a company
 * @param companyId identifies the company
 * @param companyName name of the company
 * @param dataRegisteredByDataland contains meta info for all data sets of this company
 */
data class CompanyMetaInformation(
    @field:JsonProperty(required = true) val companyId: String,
    @field:JsonProperty(required = true) val companyName: String,
    @field:JsonProperty(required = true) val dataRegisteredByDataland: MutableList<DataMetaInformation>
)
