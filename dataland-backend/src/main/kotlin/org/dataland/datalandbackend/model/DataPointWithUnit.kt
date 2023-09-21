package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.data.QualityOptions

/**
 * --- API model ---
 * Fields of a generic data point with unit and its source
 */
data class DataPointWithUnit<T>(
    override val value: T? = null,

    @field:JsonProperty(required = true)
    override val quality: QualityOptions,

    override val dataSource: CompanyReportReference? = null,

    override val comment: String? = null,

    val unit: String?,
    override val page: Long?,
    override val tagName: String?,
    override val fileName: String?,
    override val fileReference: String,
) : DataPointBaseInformationInterface, BaseDataPointInterface<T>
