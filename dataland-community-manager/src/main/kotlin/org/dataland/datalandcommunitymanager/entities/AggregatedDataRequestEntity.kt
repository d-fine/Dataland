package org.dataland.datalandcommunitymanager.entities

import org.dataland.datalandbackend.model.enums.p2p.DataRequestCompanyIdentifierType
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus

/**
 * Received entity when doing a database query to get aggregated info about the count of existing data requests
 * on Dataland.
 * @param dataTypeName contains the name of the framework for which the count is valid
 * @param dataRequestCompanyIdentifierType contains the identifier type for which the count is valid
 * @param dataRequestCompanyIdentifierValue contains the identifier value for which the count is valid
 * @param requestStatus contains the request status for which the count is valid
 * @param count the count of existing data requests for this framework, identifierType and identifierValue
 */
data class AggregatedDataRequestEntity(
    val dataTypeName: String,
    val reportingPeriod: String,
    val dataRequestCompanyIdentifierType: DataRequestCompanyIdentifierType,
    val dataRequestCompanyIdentifierValue: String,
    val requestStatus: RequestStatus,
    val count: Long,
)
