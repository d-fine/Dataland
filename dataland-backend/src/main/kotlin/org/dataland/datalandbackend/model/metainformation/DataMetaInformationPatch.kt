package org.dataland.datalandbackend.model.metainformation

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Model to update data meta information associated to data in the data store selectively
 * @param uploaderUserId the user ID of the user who uploaded the dataset
 */
data class DataMetaInformationPatch(
    @field:JsonProperty(required = true)
    val uploaderUserId: String,
)
