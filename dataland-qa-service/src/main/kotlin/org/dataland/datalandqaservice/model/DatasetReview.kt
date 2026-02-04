package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

/**
 * API model for creating a dataset review.
 */
data class DatasetReview(
    val datasetId: String,
    val companyId: String,
    val dataType: String,
    val reportingPeriod: String,
)
