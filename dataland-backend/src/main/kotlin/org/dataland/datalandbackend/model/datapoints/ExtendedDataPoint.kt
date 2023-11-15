package org.dataland.datalandbackend.model.datapoints

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.interfaces.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.data.QualityOptions

/**
 * --- API model ---
 * Fields of a generic extended data point which extends the base data point and its source
 */
data class ExtendedDataPoint<T>(
    override val value: T? = null,
    @field:JsonProperty(required = true)
    override val quality: QualityOptions,
    override val comment: String? = null,
    override val dataSource: ExtendedDocumentReference? = null,
) : ExtendedDataPoint<T>
