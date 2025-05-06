package org.dataland.datalandbackendutils.model

import org.dataland.datalandbackendutils.interfaces.DataDimensions

/**
 * Data class that contains an instance of the abstract data dimensions
 * @param companyId unique identifier to identify the company the data is associated with
 * @param dataType unique identifier for the abstract type of the data
 * @param reportingPeriod the year for which the data point was provided
 */
data class BasicDataDimensions(
    override val companyId: String,
    override val dataType: String,
    override val reportingPeriod: String,
) : DataDimensions {
    /**
     * Converts the basic data dimensions to basic data point dimensions
     * @param dataPointType the type of the data point
     * @return the basic data point dimensions
     */
    fun toBasicDataPointDimensions(dataPointType: String) = BasicDataPointDimensions(companyId, dataPointType, reportingPeriod)
}
