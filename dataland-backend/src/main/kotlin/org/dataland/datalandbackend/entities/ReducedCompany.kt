package org.dataland.datalandbackend.entities

/**
 * The entity storing data regarding a company stored in dataland
 */
interface ReducedCompany {
    val companyId: String

    val companyName: String

    val headquarters: String

    val countryCode: String

    val sector: String?

    var permId: String?
}
