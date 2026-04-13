package org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories

/**
 * Projection interface for data point QA report counts.
 * Maps query results from countByDataPointIdInGrouped to provide type-safe access to data point IDs and their active QA report counts.
 */
interface DataPointIdCountProjection {
    val dataPointId: String
    val activeQaReportCount: Long
}

