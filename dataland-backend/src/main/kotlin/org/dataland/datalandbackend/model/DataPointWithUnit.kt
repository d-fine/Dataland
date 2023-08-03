package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.data.QualityOptions

/**
 * --- API model ---
 * Fields of a generic data point with unit and its source
 */
data class DataPointWithUnit<T>(
    val value: T? = null,

    override val quality: QualityOptions,

    override val dataSource: CompanyReportReference? = null,

    override val comment: String? = null,

    val unit: String?,
) : DataPointBaseInformation(quality, dataSource, comment)
