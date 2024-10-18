package org.dataland.datalandbackend.model.companies

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.model.enums.company.IdentifierType

/**
 * --- API model ---
 * Class for defining the request body of a post company request
 * @param companyName official name of the company
 * @param companyAlternativeNames other names or abbreviations the company is known under
 * @param companyContactDetails the email addresses of the company
 * @param companyLegalForm legal structure of the company (e.g. „Public Limited Company (PLC)‟)
 * @param headquarters city where the headquarters of the company is located
 * @param headquartersPostalCode postal code of the headquarters
 * @param sector in which the company operates (e.g. Financials)
 * @param sectorCodeWz classification according to the NACE compliant WZ method
 * @param identifiers under which the company is registered (LEI, PermID, ...)
 * @param countryCode of the country of origin
 * @param isTeaserCompany flag to indicate if the company is a teaser company or not
 * @param website the url under which the company website can be reached
 * @param parentCompanyLei the lei of the parent company
 */
data class CompanyInformation(
    @field:JsonProperty(required = true)
    val companyName: String,
    val companyAlternativeNames: List<String>?,
    @field:Schema(example = "\n[\"Test@test.com\"\n]")
    val companyContactDetails: List<String>?,
    val companyLegalForm: String?,
    @field:JsonProperty(required = true)
    val headquarters: String,
    val headquartersPostalCode: String?,
    val sector: String?,
    val sectorCodeWz: String?,
    @field:JsonProperty(required = true)
    @field:Schema(
        example = "\n{\n\t\"Lei\": [\"ExampleLei\"]\n}",
    )
    val identifiers: Map<IdentifierType, List<String>>,
    @field:JsonProperty(required = true)
    val countryCode: String,
    val isTeaserCompany: Boolean?,
    val website: String?,
    val parentCompanyLei: String?,
)
