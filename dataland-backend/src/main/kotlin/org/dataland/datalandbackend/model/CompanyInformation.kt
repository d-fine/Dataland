package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Class for defining the request body of a post company request
 * @param companyName official name of the company
 * @param companyAlternativeNames other names or abbreviations the company is known under
 * @param companyLegalForm legal structure of the company (e.g. „Public Limited Company (PLC)‟)
 * @param headquarters city where the headquarters of the company is located
 * @param headquartersPostalCode postal code of the headquarters
 * @param sector in which the company operates (e.g. Financials)
 * @param identifiers under which the company is registered (LEI, PermID, ...)
 * @param countryCode of the country of origin
 * @param isTeaserCompany flag to indicate if the company is a teaser company or not
 */
data class CompanyInformation(
    @field:JsonProperty(required = true)
    val companyName: String,

    @field:JsonProperty(required = false)
    val companyAlternativeNames: List<String>? = null,

    @field:JsonProperty(required = false)
    val companyLegalForm: String? = null,

    @field:JsonProperty(required = true)
    val headquarters: String,

    @field:JsonProperty(required = false)
    val headquartersPostalCode: String? = null,

    @field:JsonProperty(required = true)
    val sector: String,

    @field:JsonProperty(required = true)
    val identifiers: List<CompanyIdentifier>,

    @field:JsonProperty(required = true)
    val countryCode: String,

    @get:JsonProperty(value = "isTeaserCompany")
    val isTeaserCompany: Boolean = false,
)
