package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus

/**
 * A filter class used in the searchDataRequestEntity-Method which allows
 * convenient usage of SEPL instructions in the query
 */
data class DataRequestsQueryFilter(
    val dataTypeFilter: String,
    val userIdFilter: String,
    val requestStatus: RequestStatus?,
    val accessStatus: AccessStatus?,
    val reportingPeriodFilter: String,
    val datalandCompanyIdFilter: String,
) {
    val dataTypeFilterLength: Int
        get() = dataTypeFilter.length

    val userIdFilterLength: Int
        get() = userIdFilter.length

    val reportingPeriodFilterLength: Int
        get() = reportingPeriodFilter.length

    val datalandCompanyIdFilterLength: Int
        get() = datalandCompanyIdFilter.length
}
