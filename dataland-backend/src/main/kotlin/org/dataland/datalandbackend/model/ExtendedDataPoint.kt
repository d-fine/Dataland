package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.interfaces.ExtendedDataPointInterface
import org.dataland.datalandbackend.model.enums.data.QualityOptions

/**
 * --- API model ---
 * Fields of a generic base data point and its source
 */
data class ExtendedDataPoint<T>(
    override val value: T? = null,
    @field:JsonProperty(required = true)
    override val quality: QualityOptions,
    override val comment: String? = null,
    val dataSource: ExtendedDocumentReference? = null,
) : ExtendedDataPointInterface<T>
