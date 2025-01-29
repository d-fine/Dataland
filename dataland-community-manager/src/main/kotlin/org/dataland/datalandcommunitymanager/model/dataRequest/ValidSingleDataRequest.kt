package org.dataland.datalandcommunitymanager.model.dataRequest

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum

/**
* Data class for the minimal information for a valid single data request
* @param companyIdentifier the company identifier for which the user wants to request framework data
* @param dataType the name of the framework for which the user wants to request framework data
* @param reportingPeriod a set of reporting periods for which the user wants to request framework data
*/
data class ValidSingleDataRequest(
    val companyIdentifier: String,
    val dataType: DataTypeEnum,
    val reportingPeriod: String,
)
