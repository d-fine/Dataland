package org.dataland.dummyeurodatclient.openApiServer.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param appId
 */
data class SafeDepositDatabaseRequestt(

    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("appId", required = true)
    val appId: kotlin.String,
) {
    // Default no-argument constructor
    constructor() : this("")
}
