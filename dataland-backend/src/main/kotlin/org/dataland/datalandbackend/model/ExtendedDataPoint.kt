package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.interfaces.ExtendedDataPointInterface
import org.dataland.datalandbackend.model.enums.data.QualityOptions

/**
 * --- API model ---
 * Fields of a generic data point with unit and its source
 */
data class ExtendedDataPoint<T>(
    override val value: T,
    @field:JsonProperty(required = true)
    override val quality: QualityOptions,
    override val comment: String,
) : ExtendedDataPointInterface<T>
