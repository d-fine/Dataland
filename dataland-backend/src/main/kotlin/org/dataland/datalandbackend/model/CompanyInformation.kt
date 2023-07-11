package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.company.IdentifierType

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
 * @param website the url under which the company website can be reached
 */
data class CompanyInformation(
    @field:JsonProperty(required = true)
    val companyName: String,

    val companyAlternativeNames: List<String>?,

    val companyLegalForm: String?,

    @field:JsonProperty(required = true)
    val headquarters: String,

    val headquartersPostalCode: String?,

    val sector: String?,

    @field:JsonProperty(required = true)
    val identifiers: Map<IdentifierType, List<String>>,

    @field:JsonProperty(required = true)
    val countryCode: String,

    // The following annotation is required (including the value field) due to a known issue with the openApi generator
    // for boolean fields starting with is
    @get:JsonProperty(value = "isTeaserCompany")
    val isTeaserCompany: Boolean = false,

    val website: String?,
)
