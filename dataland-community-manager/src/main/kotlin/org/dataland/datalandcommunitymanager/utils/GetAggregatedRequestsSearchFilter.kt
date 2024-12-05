package org.dataland.datalandcommunitymanager.utils

/**
 * A filter class used in the searchDataRequestEntity-Method which allows
 * convenient usage of SEPL instructions in the query
 */
data class GetAggregatedRequestsSearchFilter(
    val dataTypeFilter: Set<String>?,
    val requestStatus: String?,
    val priority: String?,
    val reportingPeriodFilter: String?,
    val datalandCompanyIdFilter: String?,
) {
    val dataTypeFilterLength: Int
        get() = dataTypeFilter?.size ?: 0

    val reportingPeriodFilterLength: Int
        get() = reportingPeriodFilter?.length ?: 0

    val datalandCompanyIdFilterLength: Int
        get() = datalandCompanyIdFilter?.length ?: 0

    val requestStatusLength: Int
        get() = requestStatus?.length ?: 0

    val priorityLength: Int
        get() = priority?.length ?: 0
}
