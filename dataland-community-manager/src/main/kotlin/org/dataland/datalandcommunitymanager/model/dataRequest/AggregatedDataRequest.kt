package org.dataland.datalandcommunitymanager.model.dataRequest

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum

/**
 * --- API model ---
 * Contains aggregated info about the count of existing data requests on Dataland.
 * @param dataType contains the enum of the framework
 * @param reportingPeriod reporting periods for which the user wants to request framework data
 * @param datalandCompanyId contains the companyID  stored on dataland
 * @param priority contains the request priority
 * @param requestStatus specifies the current status of the data requests
 * @param count the count of existing data requests for this framework, identifierType and identifierValue
 */
data class AggregatedDataRequest(
    val dataType: DataTypeEnum?,
    val reportingPeriod: String?,
    val datalandCompanyId: String,
    val priority: RequestPriority,
    val requestStatus: String,
    val count: Long,
)
