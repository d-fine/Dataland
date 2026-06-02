package org.dataland.datalandbackendutils.model

import org.dataland.datalandbackendutils.interfaces.BaseDimensions

/**
 * Data class that contains an instance of the base dimensions.
 *
 * @param companyId unique identifier to identify the company the data is associated with
 * @param reportingPeriod the year for which the data was provided for
 */
data class BasicBaseDimensions(
    override val companyId: String,
    override val reportingPeriod: String,
) : BaseDimensions {
    /**
     * Converts the base dimensions to dataset dimensions using the provided framework.
     *
     * @param framework the unique identifier for the framework
     * @return a BasicDatasetDimensions object with the same company ID and reporting period as the original base dimensions
     */
    fun toBasicDatasetDimensions(framework: String) =
        BasicDatasetDimensions(
            companyId = companyId,
            framework = framework,
            reportingPeriod = reportingPeriod,
        )

    /**
     * Converts the base dimensions to data point dimensions using the provided data point type.
     *
     * @param dataPointType a unique identifier for the type of data point itself
     * @return a BasicDataPointDimensions object with the same company ID and reporting period as the original base dimensions
     */
    fun toBasicDataPointDimensions(dataPointType: String) =
        BasicDataPointDimensions(
            companyId = companyId,
            dataPointType = dataPointType,
            reportingPeriod = reportingPeriod,
        )
}
