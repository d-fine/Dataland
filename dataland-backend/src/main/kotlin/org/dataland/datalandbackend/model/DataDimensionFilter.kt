package org.dataland.datalandbackend.model

/**
 * A filter class used for searching data metainformation
 * @param companyIds list of IDs to filter for
 * @param dataTypesOrDataPointTypes list of data types to filter for (can be frameworks or data point types or a mixture)
 * @param reportingPeriods list of reporting periods to filter for
 */
data class DataDimensionFilter(
    val companyIds: List<String>? = null,
    val dataTypesOrDataPointTypes: List<String>? = null,
    val reportingPeriods: List<String>? = null,
) {
    /**
     * Checks if the filter is empty (i.e. not a single filter parameter is set)
     * @return true if the filter is empty, false otherwise
     */
    fun isEmpty(): Boolean = companyIds.isNullOrEmpty() && dataTypesOrDataPointTypes.isNullOrEmpty() && reportingPeriods.isNullOrEmpty()
}
