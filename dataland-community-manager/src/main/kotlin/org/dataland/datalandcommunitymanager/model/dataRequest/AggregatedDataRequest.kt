package org.dataland.datalandcommunitymanager.model.dataRequest

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum

/**
 * --- API model ---
 * Contains aggregated info about the count of existing data requests on Dataland.
 * @param dataType contains the enum of the framework for which the count is valid
 * @param dataRequestCompanyIdentifierValue contains the identifier value for which the count is valid
 * @param count the count of existing data requests for this framework, identifierType and identifierValue
 */
data class AggregatedDataRequest(
    val dataType: DataTypeEnum?,
    val reportingPeriod: String?,
    val dataRequestCompanyIdentifierValue: String,
    val count: Long,
)
