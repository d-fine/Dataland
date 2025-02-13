package org.dataland.datalandbackendutils.interfaces

/**
 * --- API model ---
 * Interface containing the three dimensions of an abstract data object (associated company, reporting Period and which type of data it is)
 */
interface DataDimensions {
    val companyId: String
    val dataType: String
    val reportingPeriod: String
}
