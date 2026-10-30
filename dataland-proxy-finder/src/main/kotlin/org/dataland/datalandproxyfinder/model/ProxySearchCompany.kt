package org.dataland.datalandproxyfinder.model

/**
 * The company for which a proxy company is searched, enriched with the information a proxy suggestion provider
 * needs to search for a proxy.
 *
 * @param companyId the Dataland company ID of the company
 * @param companyName the name of the company
 */
data class ProxySearchCompany(
    val companyId: String,
    val companyName: String,
)
