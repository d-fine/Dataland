package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Meta information associated to an upload process of am Excel file
 * @param requestId id of the invitation request
 * @param userId id of the user responsible for the request
 * @param requestTimestamp timestamp of the request made
 */
data class RequestMetaData(
    @field:JsonProperty(required = false)
    val requestId: String,

    @field:JsonProperty(required = true)
    val userId: String,

    @field:JsonProperty(required = false)
    val uploadId: String?,

    @field:JsonProperty(required = true)
    val requestTimestamp: String,

)
