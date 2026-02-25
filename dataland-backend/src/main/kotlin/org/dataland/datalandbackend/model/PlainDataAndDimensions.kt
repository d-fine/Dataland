package org.dataland.datalandbackend.model

import org.dataland.datalandbackendutils.model.BasicDatasetDimensions

/**
 * Data class combining basic dataset dimensions with plain string data.
 */
data class PlainDataAndDimensions(
    val dimensions: BasicDatasetDimensions,
    val data: String,
)
