package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.data.QualityOptions

/**
 * --- API model ---
 * Fields of a generic data point and its source
 */
data class DataPoint<T>(
    val value: T? = null,

    // TODO this feels unneccessary. Just don't provide the datapoint if it is not there
    //  if the quality is not NA also a value must be specified
    @field:JsonProperty(required = true)
    val quality: QualityOptions,

    val dataSource: CompanyReportReference? = null,

    val comment: String? = null,
)
