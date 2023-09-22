package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.interfaces.BaseDataPointInterface

/**
 * --- API model ---
 * Fields of a generic base data point and its source
 */
data class BaseDataPoint<T>(
    @field:JsonProperty(required = true)
    override val value: T,
    override val dataSource: BaseDocumentReference? = null,
) : BaseDataPointInterface<T>
