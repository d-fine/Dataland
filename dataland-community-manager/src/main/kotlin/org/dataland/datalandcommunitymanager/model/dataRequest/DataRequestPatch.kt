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
 * @param requestStatusChangeReason The reason for the change
 */
data class DataRequestPatch(
    val requestStatus: RequestStatus? = null,
    val accessStatus: AccessStatus? = null,
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                example = "[\"testuser@example.com\"]",
            ),
    )
    val contacts: Set<String>? = null,
    val message: String? = null,
    val requestPriority: RequestPriority? = null,
    val emailOnUpdate: Boolean? = null,
    val adminComment: String? = null,
    val requestStatusChangeReason: String? = null,
)
