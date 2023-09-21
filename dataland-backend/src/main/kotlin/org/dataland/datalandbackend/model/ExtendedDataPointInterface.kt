package org.dataland.datalandbackend.model

import org.dataland.datalandbackend.model.enums.data.QualityOptions

/**
 * --- API model ---
 * Interface of the generic extended data point
 */
interface ExtendedDataPointInterface<T> : BaseDataPointInterface<T>{
    val quality: QualityOptions
    val comment: String?
}
//TODO separate those into separete files

