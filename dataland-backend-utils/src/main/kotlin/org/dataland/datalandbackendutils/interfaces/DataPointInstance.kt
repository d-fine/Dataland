package org.dataland.datalandbackendutils.interfaces

/**
 * --- API model ---
 * Interface for a basic data point instance defined by the data point dimensions plus the actual data in a JSON string
 */
interface DataPointInstance : DataPointDimensions {
    val dataPointContent: String
}
