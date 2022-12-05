package org.dataland.datalandinternalstorage.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model for Dataland-internal-EDC-API ---
 * Response model for the case that data is inserted into EuroDaT via the Dataland EDC
 * @param dataId unique identifier consisting of all information needed to identify the data (asset) in EuroDaT
 */
data class InsertDataResponse(
    @field:JsonProperty(required = true) val dataId: String,
)
