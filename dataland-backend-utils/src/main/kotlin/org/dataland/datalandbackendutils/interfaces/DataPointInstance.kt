package org.dataland.datalandbackendutils.interfaces

/**
 * Interface for a basic data point instance defined by the data point dimensions plus the actual data as a JSON string.
 */
interface DataPointInstance : DataPointDimensions {
    val dataPoint: String
}
