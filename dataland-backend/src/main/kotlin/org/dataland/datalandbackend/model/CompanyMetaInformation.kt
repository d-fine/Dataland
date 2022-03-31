package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.util.Date

/**
 * --- API model ---
 * Class for defining the meta data of a company
 * @param companyId identifies the company
 * @param companyName name of the company
 * @param headquarters city where the headquarters of the company is located
 * @param industrialSector sector in which the company operates
 * @param marketCap For publicly traded companies: The total monetary value of the outstanding shares of the company
 * @param reportingDateOfMarketCap date to which the market cap value refers
 * @param dataRegisteredByDataland contains meta info for all data sets of this company
 */
data class CompanyMetaInformation(
    @field:JsonProperty(required = true) val companyId: String,
    @field:JsonProperty(required = true) val postCompanyRequestBody: PostCompanyRequestBody,
    @field:JsonProperty(required = true) val dataRegisteredByDataland: MutableList<DataMetaInformation>
)
