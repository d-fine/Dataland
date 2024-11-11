package org.dataland.datalandcommunitymanager.model.dataRequest

import org.dataland.datalandcommunitymanager.entities.DataRequestEntity

/**
 * --- API model ---
 * Contains info about an extended stored data request on Dataland.
 * @param dataRequestId  unique identifier of the stored data request
 * @param userId the user who created this data request
 * @param creationTimestamp when the user created the data request
 * @param dataType is the enum type of the framework for which the user requested data
 * @param datalandCompanyId the value of the company identifier for this data request
 * @param companyName the name of the company for this data request
 * @param lastModifiedDate the date when the data request has been modified the last time
 * @param requestStatus the current status of the data request
 * @param accessStatus the current access status of the data request
 * @param requestPriority the priority of the data request, which can only be set by admins and viewed by anyone.
 * @param adminComment the admin comment, which can only be set and viewed by admins.
 */
data class ExtendedStoredDataRequest(
    val dataRequestId: String,
    val userId: String,
    var userEmailAddress: String?,
    val creationTimestamp: Long,
    val dataType: String,
    val reportingPeriod: String,
    val datalandCompanyId: String,
    val companyName: String,
    val lastModifiedDate: Long,
    val requestStatus: RequestStatus,
    val accessStatus: AccessStatus,
    val requestPriority: RequestPriority,
    val adminComment: String?,
) {
    constructor(dataRequestEntity: DataRequestEntity, companyName: String, userEmailAddress: String?) : this(
        dataRequestEntity.dataRequestId,
        dataRequestEntity.userId,
        userEmailAddress,
        dataRequestEntity.creationTimestamp,
        dataRequestEntity.dataType,
        dataRequestEntity.reportingPeriod,
        dataRequestEntity.datalandCompanyId,
        companyName,
        dataRequestEntity.lastModifiedDate,
        dataRequestEntity.requestStatus,
        dataRequestEntity.accessStatus,
        dataRequestEntity.requestPriority,
        dataRequestEntity.adminComment,
    )
}
