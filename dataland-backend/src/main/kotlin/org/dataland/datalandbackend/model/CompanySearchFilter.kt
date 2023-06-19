package org.dataland.datalandbackend.model

/**
 * A class containing all possible search filters for the dataland company search
 * @param searchString string used for substring matching against the company name and/or identifiers
 * @param onlyCompanyNames boolean determining if the search should be solely against the company names
 * @param dataTypeFilter if not empty, return only companies that have data reported for
 * one of the specified dataTypes
 * @param countryCodeFilter set of strings with ISO country codes to return companies whose headquarters are in
 * the country of one of those ISO country codes
 * @param sectorFilter set of strings with sector names to return companies which operate in one of those sectors
 */
data class CompanySearchFilter(
    val searchString: String = "",
    val onlyCompanyNames: Boolean = false,
    val dataTypeFilter: Set<DataType> = setOf(),
    val countryCodeFilter: Set<String> = setOf(),
    val sectorFilter: Set<String> = setOf(),
    val onlyCurrentUserAsUploader: Boolean = false,
)
