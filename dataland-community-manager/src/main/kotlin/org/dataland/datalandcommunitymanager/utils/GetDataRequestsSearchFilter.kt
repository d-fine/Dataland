package org.dataland.datalandbackend.repositories.utils

import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus

/**
 * A filter class used in the searchDataRequestEntity-Method which allows
 * convenient usage of SEPL instructions in the query
 */
data class GetDataRequestsSearchFilter(
    val dataTypeNameFilter: String,
    val userIdFilter: String,
    val requestStatus: RequestStatus?,
    val reportingPeriodFilter: String,
    val datalandCompanyIdFilter: String,
) {
    val dataTypeNameFilterLength: Int
        get() = dataTypeNameFilter.length

    val userIdFilterLength: Int
        get() = userIdFilter.length

    val reportingPeriodFilterLength: Int
        get() = reportingPeriodFilter.length

    val datalandCompanyIdFilterLength: Int
        get() = datalandCompanyIdFilter.length
}
