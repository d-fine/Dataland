package org.dataland.datalandbackend.model.proxies

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.validator.CompanyExists
import java.util.UUID

/**
 * --- API model ---
 * Class defining the proxying rules between two companies.
 *
 * @param proxiedCompanyId The company whose data may be substituted.
 * @param proxyCompanyId The company whose data may serve as a proxy.
 * @param frameworks A list of frameworks for which proxying is allowed.
 *        Empty or null means all frameworks may be proxied.
 * @param reportingPeriods A list of reporting periods (e.g. years) for which proxying is allowed.
 *        Empty or null means all reporting periods may be proxied.
 */
data class CompanyProxy(
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
        description = GeneralOpenApiDescriptionsAndExamples.PROXIED_FRAMEWORKS_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.PROXIED_FRAMEWORKS_EXAMPLE,
    )
    val frameworks: List<String>?,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXIED_REPORTING_PERIODS_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.PROXIED_REPORTING_PERIODS_EXAMPLE,
    )
    val reportingPeriods: List<String>?,
)
