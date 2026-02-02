package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.org.dataland.datalandqaservice.model

/**
 * API model for the dataset review objects returned.
 */
data class DatasetReview(
    val datasetId: String,
    val companyId: String,
    val dataType: String,
    val reportingPeriod: String,
    var reviewerUserId: String,
)
