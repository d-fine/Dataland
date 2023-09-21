package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.data.QualityOptions

/**
 * --- API model ---
 * Fields of a generic data point and its source
 */
data class DataPointOneValue<T>(
    override val value: T? = null,

    @field:JsonProperty(required = true)
    override val quality: QualityOptions,

    override val dataSource: CompanyReportReference? = null,

    override val comment: String? = null,
) : BaseDataPointInterface<T>, DataPointBaseInformationInterface
