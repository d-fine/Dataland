package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation

/**
 * --- API model ---
 * Class for defining the meta data of a company
 * @param companyId identifies the company
 * @param companyInformation contains information of company
 * @param dataRegisteredByDataland contains meta info for all datasets of this company
 */
data class StoredCompany(
    @field:JsonProperty(required = true)
    val companyId: String,
    @field:JsonProperty(required = true)
    val companyInformation: CompanyInformation,
    @field:JsonProperty(required = true)
    val dataRegisteredByDataland: List<DataMetaInformation>,
)
