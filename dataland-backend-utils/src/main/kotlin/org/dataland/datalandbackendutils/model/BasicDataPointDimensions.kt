package org.dataland.datalandbackendutils.model

import org.dataland.datalandbackendutils.interfaces.DataPointDimensions

/**
 * Data class that contains an instance of the data point dimensions
 * @param companyId unique identifier to identify the company the data is associated with
 * @param dataPointType unique identifier for the type of data point itself
 * @param reportingPeriod the year for which the data point was provided
 */
data class BasicDataPointDimensions(
    override val companyId: String,
    override val dataPointType: String,
    override val reportingPeriod: String,
) : DataPointDimensions
