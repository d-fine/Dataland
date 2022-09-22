package org.dataland.datalandbackend.repositories.utils

/**
 * A filter class used in the searchCompanies()-Method which allows
 * convenient usage of SEPL instructions in the query
 */
data class StoredCompanySearchFilter(
    val dataTypeFilter: List<String>,
    val searchString: String,
    val nameOnlyFilter: Boolean
) {
    val dataTypeFilterSize: Int
        get() = dataTypeFilter.size

    val searchStringLength: Int
        get() = searchString.length

    val searchStringLower: String
        get() = searchString.lowercase()
}
