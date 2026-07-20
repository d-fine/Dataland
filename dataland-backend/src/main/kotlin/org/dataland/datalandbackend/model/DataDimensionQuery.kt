package org.dataland.datalandbackend.model

/**
 * An internal query/filter class used for searching data metainformation.
 *
 * An empty list for any field is treated as a wildcard (no restriction on that dimension).
 *
 * @param companyIds list of IDs to filter for; empty means no restriction
 * @param dataTypes list of data types to filter for (can be frameworks or data point types or a mixture);
 *   empty means no restriction
 * @param reportingPeriods list of reporting periods to filter for; empty means no restriction
 */
data class DataDimensionQuery(
    val companyIds: List<String> = emptyList(),
    val dataTypes: List<String> = emptyList(),
    val reportingPeriods: List<String> = emptyList(),
) {
    /**
     * Checks if the query has no filter criteria set (i.e. all fields are empty).
     *
     * @return true if all fields are empty, false otherwise
     */
    fun isEmpty(): Boolean = companyIds.isEmpty() && dataTypes.isEmpty() && reportingPeriods.isEmpty()
}
