package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Framework data and the associated meta information
 * @param data a dataset of type T
 * @param metaInfo the associated meta information
 */
data class DataAndMetaInformation<T>(
    @field:JsonProperty(required = true)
    val metaInfo: DataMetaInformation,
    @field:JsonProperty(required = true)
    val data: T,
)
