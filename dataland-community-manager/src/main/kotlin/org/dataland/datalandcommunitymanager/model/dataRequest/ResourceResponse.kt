package org.dataland.datalandcommunitymanager.model.dataRequest

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.CommunityManagerOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * Contains all relevant info that a user receives regarding already existing datasets

 */
data class ResourceResponse(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.USER_PROVIDED_IDENTIFIER_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.USER_PROVIDED_IDENTIFIER_EXAMPLE,
    )
    val userProvidedIdentifier: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_NAME_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_NAME_EXAMPLE,
    )
    val companyName: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_EXAMPLE,
    )
    val framework: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    val reportingPeriod: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.RESOURCE_ID_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.RESOURCE_ID_EXAMPLE,
    )
    val resourceId: String,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.RESOURCE_URL_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.RESOURCE_URL_EXAMPLE,
    )
    val resourceUrl: String? = null,
)
