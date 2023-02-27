package org.dataland.datalandbackend.repositories.utils

/**
 * A filter class used in the searchDataMetaInformation()-Method which allows
 * convenient usage of SEPL instructions in the query
 */
data class DataMetaInformationSearchFilter(
    val companyIdFilter: String,
    val dataTypeFilter: String,
    val reportingPeriodFilter: String,
    val onlyActive: Boolean,
) {
    val companyIdFilterLength: Int
        get() = companyIdFilter.length

    val dataTypeFilterLength: Int
        get() = dataTypeFilter.length

    val reportingPeriodFilterLength: Int
        get() = reportingPeriodFilter.length
}
