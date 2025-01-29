package org.dataland.datalandbackend.model.metainformation

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Model to update data meta information associated to data in the data store selectively
 * @param uploaderUserId the user ID of the user who uploaded the dataset
 */
data class DataMetaInformationPatch(
    @field:JsonProperty(required = false)
    val uploaderUserId: String? = null,
) {
    /**
     * Returns true if all fields are set to null; false otherwise.
     * JsonIgnore is necessary, otherwise this function would be serialized.
     */
    @JsonIgnore fun isNullOrEmpty(): Boolean = uploaderUserId.isNullOrEmpty()
}
