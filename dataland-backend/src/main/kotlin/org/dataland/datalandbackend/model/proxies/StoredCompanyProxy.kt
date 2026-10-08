package org.dataland.datalandbackend.model.proxies

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples

/**
 * --- Stored API model ---
 * Class defining the stored proxying rules between two companies.
 *
 * @param proxyId The unique identifier of the stored company proxy.
 * @param childCompanyId The company whose data may be substituted.
 * @param parentCompanyId The company whose data may serve as a proxy.
 * @param validFromReportingPeriod First reporting period for which the proxy is valid.
 *        Null means there is no lower bound.
 * @param validToReportingPeriod Last reporting period for which the proxy is valid.
 *        Null means there is no upper bound.
 * @param uploadTimestamp The time (epoch milliseconds) of the last upload. Set by the system.
 * @param uploaderUserId The ID of the user who last uploaded the entry. Set by the system.
 */
data class StoredCompanyProxy(
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.PROXY_ID_EXAMPLE,
    )
    val proxyId: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.CHILD_COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val childCompanyId: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PARENT_COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val parentCompanyId: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXY_VALID_FROM_REPORTING_PERIOD_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
        nullable = true,
    )
    val validFromReportingPeriod: String?,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXY_VALID_TO_REPORTING_PERIOD_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
        nullable = true,
    )
    val validToReportingPeriod: String?,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXY_UPLOAD_TIMESTAMP_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.UPLOAD_TIME_EXAMPLE,
    )
    val uploadTimestamp: Long,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.PROXY_UPLOADER_USER_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.USER_ID_EXAMPLE,
    )
    val uploaderUserId: String,
)
