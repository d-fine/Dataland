package org.dataland.datalandbackend.interfaces.datapoints

import java.util.UUID

/**
 * --- API model ---
 * Interface containing the three dimensions of a data point (associated company, reporting Period and which data point it is)
 */
interface DataPointDimensions {
    val dataPointIdentifier: String
    val companyId: UUID
    val reportingPeriod: String
}
