package org.dataland.datasourcingservice.model.request

import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
import java.util.Date
import java.util.UUID

/**
 * DTO for creating and transferring request data.
 */
data class StoredRequest(
    val id: String,
    val companyId: String,
    val reportingPeriod: String,
    val dataType: String,
    val userId: String,
    val creationTimeStamp: Date,
    val memberComment: String? = null,
    val adminComment: String? = null,
    val lastModifiedDate: Date,
    val requestPriority: RequestPriority,
    val state: RequestState,
    val dataSourcingEntityId: String? = null,
) {
    companion object {
        /**
         * Converts a RequestEntity to a StoredDataRequest.
         *
         * @param entity The RequestEntity to convert.
         * @return The corresponding StoredDataRequest.
         */
        fun fromRequestEntity(entity: RequestEntity): StoredRequest =
            StoredRequest(
                id = entity.id.toString(),
                companyId = entity.companyId.toString(),
                reportingPeriod = entity.reportingPeriod,
                dataType = entity.dataType,
                userId = entity.userId.toString(),
                creationTimeStamp = entity.creationTimestamp,
                memberComment = entity.memberComment,
                adminComment = entity.adminComment,
                lastModifiedDate = entity.lastModifiedDate,
                requestPriority = entity.requestPriority,
                state = entity.state,
                dataSourcingEntityId = entity.dataSourcingEntity?.id.toString(),
            )
    }

    /**
     * Converts this StoredDataRequest to a RequestEntity.
     *
     * @return The corresponding RequestEntity.
     */
    fun toRequestEntity(): RequestEntity =
        RequestEntity(
            id = UUID.fromString(this.id),
            companyId = UUID.fromString(this.companyId),
            reportingPeriod = this.reportingPeriod,
            dataType = this.dataType,
            userId = UUID.fromString(this.userId),
            creationTimestamp = this.creationTimeStamp,
            memberComment = this.memberComment,
            adminComment = this.adminComment,
            lastModifiedDate = this.lastModifiedDate,
            requestPriority = this.requestPriority,
            state = this.state,
            dataSourcingEntity = null,
        )
}
