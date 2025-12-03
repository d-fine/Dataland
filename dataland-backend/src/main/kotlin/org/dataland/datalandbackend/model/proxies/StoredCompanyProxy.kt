package org.dataland.datalandbackend.model.proxies

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples

/**
 * --- Stored API model ---
 * Class defining the stored proxying rules between two companies.
 *
 * @param proxyId The unique identifier of the stored company proxy.
 * @param proxiedCompanyId The company whose data may be substituted.
 * @param proxyCompanyId The company whose data may serve as a proxy.
 * @param framework The framework for which proxying is allowed.
 *        Empty or null means all frameworks may be proxied.
 * @param reportingPeriod A reporting period for which proxying is allowed.
 *        Empty or null means all reporting periods may be proxied.
 */
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
    var proxiedCompanyId: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXY_COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    var proxyCompanyId: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXIED_FRAMEWORKS_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_FRAMEWORK_EXAMPLE,
        nullable = true,
    )
    var framework: String?,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXIED_REPORTING_PERIODS_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
        nullable = true,
    )
    var reportingPeriod: String?,
)
