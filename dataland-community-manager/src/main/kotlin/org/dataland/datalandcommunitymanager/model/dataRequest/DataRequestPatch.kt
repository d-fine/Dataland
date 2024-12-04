package org.dataland.datalandcommunitymanager.model.dataRequest

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

/** API model containing all fields that can be set when patching an existing data request
 * @param requestStatus The new request status to set
 * @param accessStatus The new access status to set
 * @param contacts The new contacts to set
 * @param message The new message to set
 * @param requestPriority The new request priority to set
 * @param adminComment The new admin comment to set
 * @return the modified data request
 */
data class DataRequestPatch(
    val requestStatus: RequestStatus?,
    val accessStatus: AccessStatus?,
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                example = "[\"testuser@example.com\"]",
            ),
    )
    val contacts: Set<String>?,
    val message: String?,
    val requestPriority: RequestPriority?,
    val adminComment: String?,
    val requestStatusChangeReason: String?,
)
