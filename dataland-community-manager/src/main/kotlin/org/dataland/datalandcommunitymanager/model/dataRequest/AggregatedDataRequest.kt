package org.dataland.datalandcommunitymanager.model.dataRequest

/**
 * --- API model ---
 * Contains aggregated info about the count of existing data requests on Dataland.
 * @param dataType contains the enum of the framework for which the count is valid
 * @param reportingPeriods reporting periods for which the user wants to request framework data
 * @param datalandCompanyIdId contains the companyID  stored on dataland
 * @param requestStatus contains the request status for which the count is valid
 * @param count the count of existing data requests for this framework, identifierType and identifierValue
 */
data class AggregatedDataRequest(
    val dataType: String?,
    val reportingPeriod: String?,
    val datalandCompanyId: String,
    val requestStatus: RequestStatus,
    val count: Long,
)
