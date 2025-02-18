package org.dataland.datalandbackend.model.datapoints

import jakarta.validation.Valid
import org.dataland.datalandbackend.interfaces.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.documents.BaseDocumentReference

/**
 * --- API model ---
 * Fields of a generic base data point and its source
 */
data class BaseDataPoint<T>(
    override val value: T?,
    @field:Valid
    override val dataSource: BaseDocumentReference? = null,
) : BaseDataPoint<T>,
    DataPointWithDocumentReference
