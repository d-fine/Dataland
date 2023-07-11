package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.model.enums.company.IdentifierType

/**
 * --- API model ---
 * Defines an update to basic company information
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
data class CompanyInformationPatch(
    val companyName: String? = null,

    val companyAlternativeNames: List<String>? = null,

    val companyLegalForm: String? = null,

    val headquarters: String? = null,

    val headquartersPostalCode: String? = null,

    val sector: String? = null,

    val identifiers: Map<IdentifierType, List<String>>? = null,

    val countryCode: String? = null,

    // The following annotation is required (including the value field) due to a known issue with the openApi generator
    // for boolean fields starting with is
    @field:JsonProperty(value = "isTeaserCompany")
    val isTeaserCompany: Boolean? = null,

    val website: String? = null,
)
