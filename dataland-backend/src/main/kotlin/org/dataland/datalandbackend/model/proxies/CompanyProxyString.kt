package org.dataland.datalandbackend.model.proxies

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.validator.CompanyExists
import org.dataland.datalandbackendutils.validator.ReportingPeriodIsValid

/**
 * --- API model ---
 * Class defining the proxying rules between two companies.
 *
 * @param proxiedCompanyId The company whose data may be substituted.
 * @param proxyCompanyId The company whose data may serve as a proxy.
 * @param framework The framework for which proxying is allowed.
 *        Empty or null means all frameworks may be proxied.
 * @param reportingPeriod A reporting period for which proxying is allowed.
 *        Empty or null means all reporting periods may be proxied.
 */
data class CompanyProxyString(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXIED_COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    @field:CompanyExists
    override val proxiedCompanyId: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXY_COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    @field:CompanyExists
    override val proxyCompanyId: String,
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
    @field:ReportingPeriodIsValid
    override val reportingPeriod: String?,
) : CompanyProxyBase
