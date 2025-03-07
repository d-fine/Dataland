package org.dataland.userservice.model

/**
 * Interface for the Portfolio API models
 */
interface Portfolio {
    val portfolioName: String
    val userId: String
    val creationTimestamp: Long
    val lastUpdateTimestamp: Long
    val companyIds: Set<String>
    val dataTypes: Set<String>
}
