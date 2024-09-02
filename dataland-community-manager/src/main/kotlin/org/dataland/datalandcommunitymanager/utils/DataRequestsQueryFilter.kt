package org.dataland.datalandcommunitymanager.utils

/**
 * A filter class used in the searchDataRequestEntity-Method which allows
 * convenient usage of SEPL instructions in the query
 */
data class DataRequestsQueryFilter(
    val dataTypeFilter: String,
    val userIdFilter: String,
    val userIdsFromEmailFilter: List<String>?,
    val requestStatus: String?,
    val accessStatus: String?,
    val reportingPeriodFilter: String,
    val datalandCompanyIdFilter: String,
) {
    val dataTypeFilterLength: Int
        get() = dataTypeFilter.length

    val userIdFilterLength: Int
        get() = userIdFilter.length

    val shouldApplyEmailFilter: Boolean
        get() = userIdsFromEmailFilter != null

    val appliedUserIdsFromEmailFilter: List<String>
        get() = userIdsFromEmailFilter ?: emptyList()

    val reportingPeriodFilterLength: Int
        get() = reportingPeriodFilter.length

    val datalandCompanyIdFilterLength: Int
        get() = datalandCompanyIdFilter.length

    val requestStatusLength: Int
        get() = requestStatus?.length ?: 0

    val accessStatusLength: Int
        get() = accessStatus?.length ?: 0
}
