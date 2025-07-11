package org.dataland.datalandcommunitymanager.model.dataRequest

import org.dataland.datalandbackend.openApiClient.model.BasicDataDimensions
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum

/**
* Data class for the minimal information for a valid single data request.
* @param companyId identifier for which company the user wants to request framework data
* @param dataType name of the framework for which the user wants to request framework data
* @param reportingPeriod reporting period for which the user wants to request framework data
*/
data class DatasetDimensions(
    val companyId: String,
    val dataType: DataTypeEnum,
    val reportingPeriod: String,
) {
    /**
     * Converts the DatasetDimensions to a BasicDataDimensions.
     */
    fun toBasicDataDimensions(): BasicDataDimensions =
        BasicDataDimensions(
            companyId = companyId,
            dataType = dataType.value,
            reportingPeriod = reportingPeriod,
        )
}
