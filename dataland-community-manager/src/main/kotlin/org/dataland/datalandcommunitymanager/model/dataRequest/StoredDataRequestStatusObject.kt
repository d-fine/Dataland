package org.dataland.datalandcommunitymanager.model.dataRequest

/**
 * --- API model ---
 * Contains info about a stored status object on Dataland.
 * @param status the data request status of the status object
 * @param creationTimestamp the creation time of the status object
 */
data class StoredDataRequestStatusObject(
    val status: RequestStatus,
    val creationTimestamp: Long,
    val accessStatus: AccessStatus,
)
