package org.dataland.datalandbackend.model

import org.dataland.datalandbackend.model.enums.data.QualityOptions

/**
 * --- API model ---
 * Fields of a generic basic data point with the minimal necessary information
 */
open class DataPointBaseInformation(
    open val quality: QualityOptions,

    open val dataSource: CompanyReportReference? = null,

    open val comment: String? = null,
)
