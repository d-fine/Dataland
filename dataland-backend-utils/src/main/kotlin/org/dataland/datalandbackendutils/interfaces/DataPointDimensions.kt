package org.dataland.datalandbackendutils.interfaces

/**
 * --- API model ---
 * Interface containing the three dimensions of a data point (associated company, reporting Period and which type of data point it is)
 */
interface DataPointDimensions {
    val companyId: String
    val dataPointType: String
    val reportingPeriod: String
}
