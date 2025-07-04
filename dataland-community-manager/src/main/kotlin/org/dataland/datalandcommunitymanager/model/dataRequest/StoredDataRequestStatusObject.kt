package org.dataland.datalandcommunitymanager.model.dataRequest

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandcommunitymanager.utils.CommunityManagerOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * Contains info about a stored status object on Dataland.
 * @param status the data request status of the status object
 * @param creationTimestamp the creation time of the status object
 * @param accessStatus the accessStatus associated with the status object
 * @param requestStatusChangeReason the reason for the status change
 * @param answeringDataId the data ID of the data set that answered the request
 */
data class StoredDataRequestStatusObject(
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.REQUEST_STATUS_DESCRIPTION,
    )
    val status: RequestStatus,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.STATUS_CREATION_TIMESTAMP_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.STATUS_CREATION_TIMESTAMP_EXAMPLE,
    )
    val creationTimestamp: Long,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.ACCESS_STATUS_DESCRIPTION,
    )
    val accessStatus: AccessStatus,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.REQUEST_STATUS_CHANGE_REASON_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.REQUEST_STATUS_CHANGE_REASON_EXAMPLE,
    )
    val requestStatusChangeReason: String?,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.ANSWERING_DATA_ID_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.ANSWERING_DATA_ID_EXAMPLE,
    )
    val answeringDataId: String?,
)
