package org.dataland.datalandcommunitymanager.entities

/**
 * Received entity when doing a database query to get aggregated info about the count of existing data requests
 * on Dataland.
 * @param dataTypeName contains the name of the framework for which the count is valid
 * @param dataRequestCompanyIdentifierType contains the identifier type for which the count is valid
 * @param datalandCompanyId contains the compandID sotred on dataland
 * @param count the count of existing data requests for this framework, identifierType and identifierValue
 */
data class AggregatedDataRequestEntity(
    val dataTypeName: String,
    val reportingPeriod: String,
    val datalandCompanyId: String,
    val count: Long,
)
