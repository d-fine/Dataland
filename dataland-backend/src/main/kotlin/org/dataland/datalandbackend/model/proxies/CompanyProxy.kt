package org.dataland.datalandbackend.model.proxies

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.ValidationUtils.convertToUUID
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.validator.CompanyExists
import org.dataland.datalandbackendutils.validator.ReportingPeriodIsValid
import java.util.UUID

/**
 * --- API model ---
 * Class defining the proxying rules between two companies.
 *
 * @param childCompanyId The company whose data may be substituted.
 * @param parentCompanyId The company whose data may serve as a proxy.
 * @param validFromReportingPeriod First reporting period for which the proxy is valid.
 *        Null means there is no lower bound.
 * @param validToReportingPeriod Last reporting period for which the proxy is valid.
 *        Null means there is no upper bound.
 */
data class CompanyProxy<IdType>(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.CHILD_COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    @field:CompanyExists
    val childCompanyId: IdType,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PARENT_COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    @field:CompanyExists
    val parentCompanyId: IdType,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXY_VALID_FROM_REPORTING_PERIOD_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
        nullable = true,
    )
    @field:ReportingPeriodIsValid
    val validFromReportingPeriod: String?,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXY_VALID_TO_REPORTING_PERIOD_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
        nullable = true,
    )
    @field:ReportingPeriodIsValid
    val validToReportingPeriod: String?,
) {
    /**
     * Converts this CompanyProxy with generic ID type to a CompanyProxy with UUIDs.
     */
    @JsonIgnore
    fun convertToCompanyProxyWithUUIDs(): CompanyProxy<UUID> =
        CompanyProxy(
            childCompanyId = convertToUUID(this.childCompanyId.toString()),
            parentCompanyId = convertToUUID(this.parentCompanyId.toString()),
            validFromReportingPeriod = this.validFromReportingPeriod,
            validToReportingPeriod = this.validToReportingPeriod,
        )
}
