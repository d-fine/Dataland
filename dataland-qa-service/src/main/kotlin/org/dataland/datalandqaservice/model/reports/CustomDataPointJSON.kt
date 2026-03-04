package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports

/**
 * Request body for patching the custom value of a data point in a dataset review.
 */
data class CustomDataPointJSON(
    val customValue: String? = null,
)
