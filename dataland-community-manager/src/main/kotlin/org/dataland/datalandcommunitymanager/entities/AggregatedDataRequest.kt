package org.dataland.datalandcommunitymanager.entities

/**
 * Received entity when doing a database query to get aggregated info about the count of existing data requests
 * on Dataland.
 * @param dataType contains the name of the framework for which the count is valid
 * @param reportingPeriod
 * @param datalandCompanyId contains the compandID sotred on dataland
 * @param requestStatus contains the request status for which the count is valid
 * @param priority contains the request priority for which the count is valid
 * @param requestStatus contains the request status for which the count is valid
 * @param count the count of existing data requests for this framework, identifierType and identifierValue
 */
interface AggregatedDataRequest {
    val dataType: String
    val reportingPeriod: String
    val datalandCompanyId: String
    val priority: String
    val requestStatus: String
    val count: Long
}
