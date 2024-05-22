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
 */
data class ExtendedStoredDataRequest(
    val dataRequestId: String,

    val userId: String,

    val creationTimestamp: Long,

    val dataType: String,

    val reportingPeriod: String,

    val datalandCompanyId: String,

    val companyName: String,

    val lastModifiedDate: Long,

    val requestStatus: RequestStatus,
) {
    constructor(dataRequestEntity: DataRequestEntity, companyName: String) : this(
        dataRequestEntity.dataRequestId,
        dataRequestEntity.userId,
        dataRequestEntity.creationTimestamp,
        dataRequestEntity.dataType,
        dataRequestEntity.reportingPeriod,
        dataRequestEntity.datalandCompanyId,
        companyName,
        dataRequestEntity.lastModifiedDate,
        dataRequestEntity.requestStatus,
    )
}
