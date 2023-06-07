package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Fields of a generic base data point and its source
 */
data class BaseDataPoint<T>(
    @field:JsonProperty(required = true)
    val value: T,

    val dataSource: DocumentReference? = null,
)
