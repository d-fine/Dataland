package org.dataland.datalandbackend.entities

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples

/**
 * Just the basic data regarding a company stored in dataland
 */
data class BasicCompanyInformation(
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val companyId: String,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.COMPANY_NAME_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.COMPANY_NAME_EXAMPLE,
    )
    val companyName: String,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.HEADQUARTERS_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.HEADQUARTERS_EXAMPLE,
    )
    val headquarters: String,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.COUNTRY_CODE_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.COUNTRY_CODE_EXAMPLE,
    )
    val countryCode: String,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.SECTOR_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.SECTOR_EXAMPLE,
    )
    val sector: String?,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.LEI_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.LEI_EXAMPLE,
    )
    val lei: String?,
)
