package org.dataland.datalandbackendutils.interfaces

import org.dataland.datalandbackendutils.model.DatasetType

/**
 * Interface containing the three dimensions of a dataset (associated company, reporting Period and which framework it is)
 */
interface DatasetDimensions : BaseDimensions {
    val framework: DatasetType
}
