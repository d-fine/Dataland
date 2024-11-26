package org.dataland.datalandbackendutils.interfaces

/**
 * --- API model ---
 * Interface containing the three dimensions of a data point (associated company, reporting Period and which data point it is)
 */
interface DataPointDimensions {
    val dataPointIdentifier: String
    val companyId: String
    val reportingPeriod: String
}
