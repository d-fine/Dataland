package org.dataland.datalanduserservice.model

/**
 * Interface for the Portfolio API models
 */
interface Portfolio {
    val portfolioName: String
    val companyIds: Set<String>
    val dataTypes: Set<String>
}
