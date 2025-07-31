package org.dataland.datalandbackend.model.companies

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples

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
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_NAME_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_NAME_EXAMPLE,
    )
    val companyName: String,
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = BackendOpenApiDescriptionsAndExamples.COMPANY_ALTERNATIVE_NAMES_DESCRIPTION,
                example = BackendOpenApiDescriptionsAndExamples.COMPANY_ALTERNATIVE_NAMES_EXAMPLE,
            ),
    )
    val companyAlternativeNames: List<String>? = null,
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = BackendOpenApiDescriptionsAndExamples.COMPANY_CONTACT_DETAILS_DESCRIPTION,
                example = BackendOpenApiDescriptionsAndExamples.COMPANY_CONTACT_DETAILS_EXAMPLE,
            ),
    )
    val companyContactDetails: List<String>? = null,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.COMPANY_LEGAL_FORM_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.COMPANY_LEGAL_FORM_EXAMPLE,
    )
    val companyLegalForm: String? = null,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.HEADQUARTERS_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.HEADQUARTERS_EXAMPLE,
    )
    val headquarters: String,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.HEADQUARTERS_POSTAL_CODE_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.HEADQUARTERS_POSTAL_CODE_EXAMPLE,
    )
    val headquartersPostalCode: String? = null,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.SECTOR_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.SECTOR_EXAMPLE,
    )
    val sector: String? = null,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.SECTOR_CODE_WZ_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.SECTOR_CODE_WZ_EXAMPLE,
    )
    val sectorCodeWz: String? = null,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.IDENTIFIERS_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.IDENTIFIERS_EXAMPLE,
    )
    val identifiers: Map<IdentifierType, List<String>>,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.COUNTRY_CODE_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.COUNTRY_CODE_EXAMPLE,
    )
    val countryCode: String,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.IS_TEASER_COMPANY_DESCRIPTION,
    )
    val isTeaserCompany: Boolean? = null,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.WEBSITE_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.WEBSITE_EXAMPLE,
    )
    val website: String? = null,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.PARENT_COMPANY_LEI_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.PARENT_COMPANY_LEI_EXAMPLE,
    )
    val parentCompanyLei: String? = null,
) {
    /**
     * Converts this [CompanyInformation] to a [CompanyInformationPatch].
     */
    fun toCompanyInformationPatch(): CompanyInformationPatch =
        CompanyInformationPatch(
            companyName = companyName,
            companyAlternativeNames = companyAlternativeNames,
            companyContactDetails = companyContactDetails,
            companyLegalForm = companyLegalForm,
            headquarters = headquarters,
            headquartersPostalCode = headquartersPostalCode,
            sector = sector,
            sectorCodeWz = sectorCodeWz,
            identifiers = identifiers,
            countryCode = countryCode,
            isTeaserCompany = isTeaserCompany,
            website = website,
            parentCompanyLei = parentCompanyLei,
        )
}
