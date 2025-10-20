package org.dataland.datalandbackendutils.interfaces

/**
 * General interface containing all three dimensions of an abstract data object where the data type can either
 * represent a data point or a framework.
 */
interface DataDimensions : BaseDimensions {
    val dataType: String
}
