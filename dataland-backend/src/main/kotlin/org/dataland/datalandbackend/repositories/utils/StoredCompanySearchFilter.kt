package org.dataland.datalandbackend.repositories.utils

/**
 * A filter class used in the searchCompanies()-Method which allows
 * convenient usage of SEPL instructions in the query
 */
data class StoredCompanySearchFilter(
    val dataTypeFilter: List<String> = listOf(),
    val countryCodeFilter: List<String> = listOf(),
    val sectorFilter: List<String> = listOf(),
    val searchString: String = "",
    val nameOnlyFilter: Boolean = false,
    val uploaderIdFilter: List<String> = listOf(),
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
