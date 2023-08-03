package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.data.QualityOptions

/**
 * --- API model ---
 * Fields of a generic data point and its source
 */
open class DataPointBaseInformation(
    open val quality: QualityOptions,

    open val dataSource: CompanyReportReference? = null,

    open val comment: String? = null,
)
