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
    override val quality: QualityOptions,

    override val dataSource: CompanyReportReference? = null,

    override val comment: String? = null,

    val valueAsAbsolute: T? = null,

) : DataPointBaseInformation(quality, dataSource, comment)
