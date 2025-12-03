package org.dataland.datalandbackend.model.proxies

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import java.util.UUID

/**
 * Class defining the proxying rules between two companies with UUID.
 *
 * @param proxiedCompanyId The company whose data may be substituted.
 * @param proxyCompanyId The company whose data may serve as a proxy.
 * @param framework The framework for which proxying is allowed.
 *        Empty or null means all frameworks may be proxied.
 * @param reportingPeriod A reporting period for which proxying is allowed.
 *        Empty or null means all reporting periods may be proxied.
 */
data class CompanyProxyUUID(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXIED_COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    override val proxiedCompanyId: UUID,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXY_COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    override val proxyCompanyId: UUID,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXIED_FRAMEWORKS_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_FRAMEWORK_EXAMPLE,
        nullable = true,
    )
    override val framework: String?,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXIED_REPORTING_PERIODS_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
        nullable = true,
    )
    override val reportingPeriod: String?,
) : CompanyProxyBase
