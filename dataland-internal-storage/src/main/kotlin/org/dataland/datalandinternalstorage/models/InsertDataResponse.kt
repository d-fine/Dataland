package org.dataland.datalandinternalstorage.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model for Dataland-internal-storage-API ---
 * Response model for the case that data is inserted into the internal storage via the Dataland
 * @param dataId unique identifier consisting of all information needed to identify the data (asset) in the storage
 */
data class InsertDataResponse(
    @field:JsonProperty(required = true) val dataId: String,
)
