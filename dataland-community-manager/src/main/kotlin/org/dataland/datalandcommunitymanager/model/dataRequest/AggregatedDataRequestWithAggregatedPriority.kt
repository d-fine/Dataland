package org.dataland.datalandcommunitymanager.model.dataRequest

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.utils.CommunityManagerOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * Contains aggregated info about the count of existing data requests on Dataland.
 * @param dataType contains the enum of the framework
 * @param reportingPeriod reporting periods for which the user wants to request framework data
 * @param datalandCompanyId contains the companyID  stored on dataland
 * @param aggregatedPriority contains the aggregated request priority
 * @param count the count of existing data requests for this framework, identifierType and identifierValue
 */
data class AggregatedDataRequestWithAggregatedPriority(
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
    )
    val dataType: DataTypeEnum?,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    val reportingPeriod: String?,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val datalandCompanyId: String,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.AGGREGATED_DATA_REQUEST_PRIORITY_DESCRIPTION,
    )
    val aggregatedPriority: AggregatedRequestPriority,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.DATA_REQUEST_COUNT_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.DATA_REQUEST_COUNT_EXAMPLE,
    )
    val count: Long,
)
