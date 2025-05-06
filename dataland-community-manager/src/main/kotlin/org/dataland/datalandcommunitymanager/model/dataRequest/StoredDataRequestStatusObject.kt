package org.dataland.datalandcommunitymanager.model.dataRequest

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
    val status: RequestStatus,
    val creationTimestamp: Long,
    val accessStatus: AccessStatus,
    val requestStatusChangeReason: String?,
    val answeringDataId: String?,
)
