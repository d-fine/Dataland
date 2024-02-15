package org.dataland.datalandbackend.repositories.utils

import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus

/**
 * A filter class used in the searchDataRequestEntity-Method which allows
 * convenient usage of SEPL instructions in the query
 */
data class GetDataRequestsSearchFilter(
    val dataTypeFilter: String,
    val userIdFilter: String,
    val requestStatus: RequestStatus?,
    val reportingPeriodFilter: String,
    val dataRequestCompanyIdentifierValueFilter: String,
) {
    val dataTypeFilterLength: Int
        get() = dataTypeFilter.length

    val userIdFilterLength: Int
        get() = userIdFilter.length

    val reportingPeriodFilterLength: Int
        get() = reportingPeriodFilter.length

    val dataRequestCompanyIdentifierValueFilterLength: Int
        get() = dataRequestCompanyIdentifierValueFilter.length
}
