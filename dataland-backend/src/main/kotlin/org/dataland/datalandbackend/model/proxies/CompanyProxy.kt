package org.dataland.datalandbackend.model.proxies

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.validator.DataTypeIsValid
import org.dataland.datalandbackendutils.utils.ValidationUtils.convertToUUID
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.validator.CompanyExists
import org.dataland.datalandbackendutils.validator.ReportingPeriodIsValid
import java.util.UUID

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
data class CompanyProxy<IdType>(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXIED_COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    @field:CompanyExists
    val proxiedCompanyId: IdType,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXY_COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    @field:CompanyExists
    val proxyCompanyId: IdType,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXIED_FRAMEWORKS_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_FRAMEWORK_EXAMPLE,
        nullable = true,
    )
    @field:DataTypeIsValid
    val framework: String?,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXIED_REPORTING_PERIODS_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
        nullable = true,
    )
    @field:ReportingPeriodIsValid
    val reportingPeriod: String?,
) {
    /**
     * Converts this CompanyProxy with generic ID type to a CompanyProxy with UUIDs.
     */
    @JsonIgnore
    fun convertToCompanyProxyWithUUIDs(): CompanyProxy<UUID> =
        CompanyProxy(
            proxiedCompanyId = convertToUUID(this.proxiedCompanyId.toString()),
            proxyCompanyId = convertToUUID(this.proxyCompanyId.toString()),
            framework = this.framework,
            reportingPeriod = this.reportingPeriod,
        )
}
