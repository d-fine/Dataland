package org.dataland.datalandbackend.model.datapoints

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.interfaces.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.documents.BaseDocumentReference

/**
 * --- API model ---
 * Fields of a generic base data point and its source
 */
data class BaseDataPoint<T>(
    @field:JsonProperty(required = true)
    override val value: T,
    override val dataSource: BaseDocumentReference? = null,
) : BaseDataPoint<T>
