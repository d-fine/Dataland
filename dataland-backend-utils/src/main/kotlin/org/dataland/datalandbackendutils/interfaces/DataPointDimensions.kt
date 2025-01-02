package org.dataland.datalandbackendutils.interfaces

/**
 * --- API model ---
 * Interface containing the three dimensions of a data point (associated company, reporting Period and which data point it is)
 */
interface DataPointDimensions {
    val companyId: String
    val dataPointIdentifier: String
    val reportingPeriod: String
}
