package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.data.QualityOptions

/**
 * --- API model ---
 * Fields of a big decimal data point containing absolute and percentage values and their source
 */
data class DataPointAbsoluteAndPercentage<T> (
    val valueAsPercentage: T? = null,

    @field:JsonProperty(required = true)
    val quality: QualityOptions,

    val dataSource: CompanyReportReference? = null,

    val comment: String? = null,

    val valueAsAbsolute: T? = null,

)
