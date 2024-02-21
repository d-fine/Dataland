package org.dataland.datalandcommunitymanager.entities

import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus

/**
 * Received entity when doing a database query to get aggregated info about the count of existing data requests
 * on Dataland.
 * @param dataTypeName contains the name of the framework for which the count is valid
 * @param reportingPeriod
 * @param datalandCompanyId contains the compandID sotred on dataland
 * @param requestStatus contains the request status for which the count is valid
 * @param count the count of existing data requests for this framework, identifierType and identifierValue
 */
data class AggregatedDataRequestEntity(
    val dataTypeName: String,
    val reportingPeriod: String,
    val datalandCompanyId: String,
    val requestStatus: RequestStatus,
    val count: Long,
)
