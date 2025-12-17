package org.dataland.datalandbackendutils.model

/**
 * Generalization of DataDimensions to lists
 */
class ListDataDimensions(
    val companyIds: List<String>,
    val reportingPeriods: List<String>,
    val dataTypes: List<String>,
)
