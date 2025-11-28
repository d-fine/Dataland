package org.dataland.datalandbackend.model.proxies

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.validator.CompanyExists
import java.util.UUID

data class CompanyProxy(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.PROXY_ID_EXAMPLE,
    )
    val proxyId: UUID,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXIED_COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    @field:CompanyExists
    val proxiedCompanyId: UUID,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXY_COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    @field:CompanyExists
    val proxyCompanyId: UUID,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXIED_FRAMEWORK_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.PROXIED_FRAMEWORK_EXAMPLE,
    )
    val framework: String?,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXIED_REPORTING_PERIOD_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.PROXIED_REPORTING_PERIOD_EXAMPLE,
    )
    val reportingPeriod: String?,
)
