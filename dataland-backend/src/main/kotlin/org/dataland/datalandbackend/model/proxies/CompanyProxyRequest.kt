package org.dataland.datalandbackend.model.proxies

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.validator.CompanyExists
import java.util.UUID

/**
 * --- API model ---
 * Request body for POST /company-proxies.
 *
 * Represented entirely as Strings to hide UUIDs from external consumers.
 *
 * @param proxiedCompanyId The company whose data may be substituted.
 * @param proxyCompanyId The company whose data may serve as a proxy.
 * @param framework A list of frameworks for which proxying is allowed.
 *        Empty or null means all frameworks may be proxied.
 * @param reportingPeriod A list of reporting periods (e.g. years).
 *        Empty or null means all reporting periods may be proxied.
 */
data class CompanyProxyRequest(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXIED_COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    @field:CompanyExists
    val proxiedCompanyId: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXY_COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    @field:CompanyExists
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

/**
 * Convert API Request → Domain Model
 */
fun CompanyProxyRequest.toDomainModel(): CompanyProxy =
    CompanyProxy(
        proxiedCompanyId = UUID.fromString(this.proxiedCompanyId),
        proxyCompanyId = UUID.fromString(this.proxyCompanyId),
        framework = this.framework,
        reportingPeriod = this.reportingPeriod,
    )
