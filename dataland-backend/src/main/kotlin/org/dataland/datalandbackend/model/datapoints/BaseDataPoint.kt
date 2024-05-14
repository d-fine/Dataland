package org.dataland.datalandbackend.model.datapoints

import org.dataland.datalandbackend.interfaces.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.documents.BaseDocumentReference
import org.dataland.datalandbackend.validator.DocumentExists

/**
 * --- API model ---
 * Fields of a generic base data point and its source
 */
data class BaseDataPoint<T>(
    override val value: T?,
    @field:DocumentExists
    override val dataSource: BaseDocumentReference? = null,
) : BaseDataPoint<T>
