package org.dataland.datalandbackend.model

import org.dataland.datalandbackend.model.enums.data.QualityOptions

/**
 * --- API model ---
 * Fields of a generic data point and its source
 */
data class DataPoint<T>(
    val value: T? = null,

    val quality: QualityOptions? = null,

    val dataSource: CompanyReportReference? = null,

    val comment: String? = null,
)
