package org.dataland.datalandcompanyservice.model

/**
 * The company for which a proxy company is searched, enriched with the information a proxy suggestion provider
 * needs to search for a proxy.
 *
 * @param companyId the Dataland company ID of the company
 * @param companyName the name of the company
 * @param alternativeNames other names the company is known under on Dataland
 * @param identifiers the identifiers of the company on Dataland by identifier type (e.g. Lei)
 * @param countryCode the country code of the company
 * @param headquarters the city of the headquarters of the company
 * @param parentCompanyLei the LEI of the parent company stored on Dataland (usually imported from GLEIF)
 * @param parentCompany the company on Dataland with the parentCompanyLei, if it exists
 */
data class ProxySearchCompany(
    val companyId: String,
    val companyName: String,
    val alternativeNames: List<String> = emptyList(),
    val identifiers: Map<String, List<String>> = emptyMap(),
    val countryCode: String? = null,
    val headquarters: String? = null,
    val parentCompanyLei: String? = null,
    val parentCompany: DatalandCompanyCandidate? = null,
)
