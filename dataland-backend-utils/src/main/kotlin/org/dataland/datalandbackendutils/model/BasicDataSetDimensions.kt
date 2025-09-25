package org.dataland.datalandbackendutils.model

import org.dataland.datalandbackendutils.interfaces.DataSetDimensions

/**
 * Data class that contains an instance of the data point dimensions
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
     * Converts the data set dimensions to data point dimensions by providing a list of data point types
     *
     * @param dataPointTypes a list of data point types
     * @return a list of data point dimensions with the same reporting period and company ID as the original data set dimension
     */
    fun toBasicDataPointDimensions(dataPointTypes: Collection<String>) =
        dataPointTypes.map { BasicDataPointDimensions(companyId, it, reportingPeriod) }

    fun toBasicDataDimensions() = BasicDataDimensions(companyId, framework, reportingPeriod)

    fun toBaseDimensions(): BasicBaseDimensions = BasicBaseDimensions(companyId = companyId, reportingPeriod = reportingPeriod)
}
