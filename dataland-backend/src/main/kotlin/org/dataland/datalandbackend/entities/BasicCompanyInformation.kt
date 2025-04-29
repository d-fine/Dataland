package org.dataland.datalandbackend.entities

/**
 * Just the basic data regarding a company stored in dataland
 */
data class BasicCompanyInformation(
    val companyId: String,
    val companyName: String,
    val headquarters: String,
    val countryCode: String,
    val sector: String?,
    val lei: String?,
)
