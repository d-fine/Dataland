package org.dataland.datalandbackend.model.metainformation

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Framework data and the associated meta information
 *  @param data as a plain JSON string
 *  @param metaInfo the associated meta information
 */
data class PlainDataAndMetaInformation(
    @field:JsonProperty(required = true)
    val metaInfo: DataMetaInformation,
    @field:JsonProperty(required = true)
    val data: String,
)
