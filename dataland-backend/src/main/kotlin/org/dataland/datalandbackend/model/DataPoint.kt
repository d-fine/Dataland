package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.data.QualityOptions

/**
 * --- API model ---
 * Fields of a generic data point and its source
 */
data class DataPoint<T>(
    val value: T? = null,

    @field:JsonProperty(required = true)
    val quality: QualityOptions,

    val dataSource: CompanyReportReference? = null,

    val comment: String? = null,
)
