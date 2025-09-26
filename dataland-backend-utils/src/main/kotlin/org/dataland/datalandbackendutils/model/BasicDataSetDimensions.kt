package org.dataland.datalandbackendutils.model

import org.dataland.datalandbackendutils.interfaces.DataSetDimensions

/**
 * Data class that contains an instance of the dataset dimensions
 * @param companyId unique identifier to identify the company the data is associated with
 * @param framework unique identifier for the framework
 * @param reportingPeriod the year for which the data point was provided
 */
data class BasicDataSetDimensions(
    override val companyId: String,
    override val framework: String,
    override val reportingPeriod: String,
) : DataSetDimensions {
    /**
     * Converts the dataset dimensions to data point dimensions by providing a list of data point types
     * @param dataPointTypes a list of data point types
     * @return a list of data point dimensions with the same reporting period and company ID as the original dataset dimensions
     */
    fun toBasicDataPointDimensions(dataPointTypes: Collection<String>) =
        dataPointTypes.map { BasicDataPointDimensions(companyId, it, reportingPeriod) }

    /**
     * Converts the dataset dimensions to the abstract basic data dimensions by using the framework as data type
     * @return a BasicDataDimensions object with the same company ID, framework, and reporting period as the original dataset dimensions
     */
    fun toBasicDataDimensions() = BasicDataDimensions(companyId, framework, reportingPeriod)

    /**
     * Converts the dataset dimensions to base dimensions by dropping the framework
     * @return a BasicBaseDimensions object with the same company ID and reporting period as the original dataset dimensions
     */
    fun toBaseDimensions(): BasicBaseDimensions = BasicBaseDimensions(companyId = companyId, reportingPeriod = reportingPeriod)
}
