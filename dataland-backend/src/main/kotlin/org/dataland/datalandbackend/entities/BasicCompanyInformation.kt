package org.dataland.datalandbackend.entities

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.utils.CompanyControllerDescriptionsAndExamples

/**
 * Just the basic data regarding a company stored in dataland
 */
data class BasicCompanyInformation(
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = CompanyControllerDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val companyId: String,
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.COMPANY_NAME_DESCRIPTION,
        example = CompanyControllerDescriptionsAndExamples.COMPANY_NAME_EXAMPLE,
    )
    val companyName: String,
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.HEADQUARTERS_DESCRIPTION,
        example = CompanyControllerDescriptionsAndExamples.HEADQUARTERS_EXAMPLE,
    )
    val headquarters: String,
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.COUNTRY_CODE_DESCRIPTION,
        example = CompanyControllerDescriptionsAndExamples.COUNTRY_CODE_EXAMPLE,
    )
    val countryCode: String,
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.SECTOR_DESCRIPTION,
        example = CompanyControllerDescriptionsAndExamples.SECTOR_EXAMPLE,
    )
    val sector: String?,
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.LEI_DESCRIPTION,
        example = CompanyControllerDescriptionsAndExamples.LEI_EXAMPLE,
    )
    val lei: String?,
)
