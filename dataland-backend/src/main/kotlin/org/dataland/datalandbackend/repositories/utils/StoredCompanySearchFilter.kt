package org.dataland.datalandbackend.repositories.utils

import org.dataland.datalandbackend.model.enums.company.StockIndex

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