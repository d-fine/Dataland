package org.dataland.datalandcommunitymanager.model.dataRequest

/**
 * --- API model ---
 * Contains info about a stored data request on Dataland.
 * @param dataRequestId  unique identifier of the stored data request
 * @param userId the user who created this data request
 * @param userEmailAddress contains the email address of the user who created this data request
 * @param creationTimestamp when the user created the data request
 * @param dataType is the enum type of the framework for which the user requested data
 * @param datalandCompanyId the value of the company identifier for this data request
 * @param lastModifiedDate the date when the data request has been modified the last time
 * @param messageHistory a list of all message objects which were created during the life cycle
 * @param requestStatus the current status of the data request
 */
data class StoredDataRequest(
    val dataRequestId: String,
    val userId: String,
    val userEmailAddress: String?,
    val creationTimestamp: Long,
    val dataType: String,
    val reportingPeriod: String,
    val datalandCompanyId: String,
    val messageHistory: List<StoredDataRequestMessageObject>,
    val dataRequestStatusHistory: List<StoredDataRequestStatusObject>,
    val lastModifiedDate: Long,
    val requestStatus: RequestStatus,
    val accessStatus: AccessStatus,
)
