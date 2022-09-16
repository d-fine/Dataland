package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of a generic data point and its source
 */
data class DataPoint(
    @field:JsonProperty("value")
    val value: BigDecimal? = null,

    @field:JsonProperty("quality", required = true)
    val quality: QualityOptions,

    @field:JsonProperty("dataSource")
    val dataSource: CompanyReportReference? = null
)
