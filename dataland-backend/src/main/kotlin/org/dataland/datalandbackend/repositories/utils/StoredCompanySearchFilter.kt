package org.dataland.datalandbackend.repositories.utils

import org.dataland.datalandbackend.model.enums.company.StockIndex

/**
 * A filter class used in the searchCompanies()-Method which allows
 * convenient usage of SEPL instructions in the query
 */
data class StoredCompanySearchFilter(
    val dataTypeFilter: List<String>,
    val stockIndexFilter: List<StockIndex>,
    val searchString: String,
    val nameOnlyFilter: Boolean
) {
    val dataTypeFilterSize: Int
        get() = dataTypeFilter.size

    val stockIndexFilterSize: Int
        get() = stockIndexFilter.size

    val searchStringLength: Int
        get() = searchString.length

    val searchStringLower: String
        get() = searchString.lowercase()
}
