package org.dataland.datalandbackend.model.datapoints

import jakarta.validation.Valid
import org.dataland.datalandbackend.interfaces.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackend.validator.QualityAndValue

/**
 * --- API model ---
 * Fields of a generic extended data point which extends the base data point and its source
 */
@QualityAndValue
data class ExtendedDataPoint<T> (
    override val value: T? = null,
    override val quality: QualityOptions? = null,
    override val comment: String? = null,
    @field:Valid
    override val dataSource: ExtendedDocumentReference? = null,
) : ExtendedDataPoint<T>
