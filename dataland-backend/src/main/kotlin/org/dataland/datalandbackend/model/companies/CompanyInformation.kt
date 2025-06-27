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
    @field:Schema(description = "The official name of the company", example = "ABC Corporation")
    val companyName: String,
    @field:Schema(description = "Any alternative names or abbreviations the company might be known by", example = "\n[\"ABC Corp.\"\n]")
    val companyAlternativeNames: List<String>?,
    @field:Schema(description = "The email addresses of the company", example = "\n[\"contact@abccorp.com\"\n]")
    val companyContactDetails: List<String>?,
    @field:Schema(description = "The legal structure or from under which the company operates", example = "Private Limited Company (Ltd)")
    val companyLegalForm: String?,
    @field:JsonProperty(required = true)
    @field:Schema(description = "The city where the main office of the company is located", example = "Berlin")
    val headquarters: String,
    @field:Schema(description = "The postal code of the headquarters", example = "10123")
    val headquartersPostalCode: String?,
    @field:Schema(description = "The industry or sector in which the company operates", example = "Information Technology")
    val sector: String?,
    @field:Schema(description = "The industry classification code according to the NACE compliant WZ method", example = "62.10.4")
    val sectorCodeWz: String?,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = "Unique identifiers associated with the company, such as LEI, PermId, ...",
        example = "\n{\n\t\"Lei\": [\"5493001KJX4BT0IHAG73\"]\n}",
    )
    val identifiers: Map<IdentifierType, List<String>>,
    @field:JsonProperty(required = true)
    @field:Schema(description = "The ISO 3166-1 alpha-2 code representing the country of origin", example = "DE")
    val countryCode: String,
    @field:Schema(description = "A boolean indicating if the company is a teaser company", example = "true")
    val isTeaserCompany: Boolean?,
    @field:Schema(description = "The official website URL of the company", example = "www.abccorp.com")
    val website: String?,
    @field:Schema(description = "The LEI of the parent company, if applicable", example = "null")
    val parentCompanyLei: String?,
)
