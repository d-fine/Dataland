package org.dataland.datalandbackend.model

/**
 * A filter class used for searching data metainformation
 * @param companyIds list of IDs to filter for
 * @param dataTypes list of data types to filter for (can be either frameworks or data point types)
 * @param reportingPeriods list of reporting periods to filter for
 */
data class DataDimensionFilter(
    val companyIds: List<String>? = null,
    val dataTypes: List<String>? = null,
    val reportingPeriods: List<String>? = null,
) {
    fun isEmpty(): Boolean = companyIds.isNullOrEmpty() && dataTypes.isNullOrEmpty() && reportingPeriods.isNullOrEmpty()
}
