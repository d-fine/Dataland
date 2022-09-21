package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.data.QualityOptions

/**
 * --- API model ---
 * Fields of a generic data point and its source
 */
data class DataPoint <T>(
    @field:JsonProperty("value")
    val value: T? = null,

    @field:JsonProperty("quality", required = true)
    val quality: QualityOptions,

    @field:JsonProperty("dataSource")
    val dataSource: CompanyReportReference? = null
)
