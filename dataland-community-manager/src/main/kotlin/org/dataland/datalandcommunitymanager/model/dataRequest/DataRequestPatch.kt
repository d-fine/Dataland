package org.dataland.datalandcommunitymanager.model.dataRequest

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.CommunityManagerOpenApiDescriptionsAndExamples

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
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.REQUEST_STATUS_DESCRIPTION,
    )
    val requestStatus: RequestStatus? = null,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.ACCESS_STATUS_DESCRIPTION,
    )
    val accessStatus: AccessStatus? = null,
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = CommunityManagerOpenApiDescriptionsAndExamples.CONTACTS_DESCRIPTION,
                example = CommunityManagerOpenApiDescriptionsAndExamples.CONTACTS_EXAMPLE,
            ),
    )
    val contacts: Set<String>? = null,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.POST_MESSAGE_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.POST_MESSAGE_EXAMPLE,
    )
    val message: String? = null,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.REQUEST_PRIORITY_DESCRIPTION,
    )
    val requestPriority: RequestPriority? = null,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.BULK_REQUEST_NOTIFY_ME_IMMEDIATELY_DESCRIPTION,
    )
    var notifyMeImmediately: Boolean? = null,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.ADMIN_COMMENT_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.ADMIN_COMMENT_EXAMPLE,
    )
    val adminComment: String? = null,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.REQUEST_STATUS_CHANGE_REASON_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.REQUEST_STATUS_CHANGE_REASON_EXAMPLE,
    )
    val requestStatusChangeReason: String? = null,
)
