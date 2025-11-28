package org.dataland.datalandbackend.model.proxies

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples

// Single row of what got stored in company_proxy_relations
data class StoredCompanyProxy(
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.PROXY_ID_EXAMPLE,
    )
    val proxyId: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXIED_COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val proxiedCompanyId: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXY_COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val proxyCompanyId: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXIED_FRAMEWORKS_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_FRAMEWORK_EXAMPLE,
        nullable = true,
    )
    val framework: String?,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXIED_REPORTING_PERIODS_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
        nullable = true,
    )
    val reportingPeriod: String?,
)
