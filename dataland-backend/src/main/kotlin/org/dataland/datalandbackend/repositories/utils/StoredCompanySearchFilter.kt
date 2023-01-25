package org.dataland.datalandbackend.repositories.utils

/**
 * A filter class used in the searchCompanies()-Method which allows
 * convenient usage of SEPL instructions in the query
 */
data class StoredCompanySearchFilter(
    val dataTypeFilter: List<String>,
    val countryCodeFilter: List<String>,
    val sectorFilter: List<String>,
    val searchString: String,
    val nameOnlyFilter: Boolean,
    val uploaderIdFilter: List<String>
) {
    val dataTypeFilterSize: Int
        get() = dataTypeFilter.size

    val countryCodeFilterSize: Int
        get() = countryCodeFilter.size

    val sectorFilterSize: Int
        get() = sectorFilter.size

    val searchStringLength: Int
        get() = searchString.length

    val searchStringLower: String
        get() = searchString.lowercase()

    val uploaderIdFilterSize: Int
        get() = uploaderIdFilter.size
}
