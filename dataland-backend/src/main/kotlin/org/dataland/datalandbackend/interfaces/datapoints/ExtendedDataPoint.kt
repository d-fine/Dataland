package org.dataland.datalandbackend.interfaces.datapoints

import org.dataland.datalandbackend.model.enums.data.QualityOptions

/**
 * --- API model ---
 * Interface of the generic extended data point
 */
interface ExtendedDataPoint<T> : BaseDataPoint<T> {
    val quality: QualityOptions
    val comment: String?
}
