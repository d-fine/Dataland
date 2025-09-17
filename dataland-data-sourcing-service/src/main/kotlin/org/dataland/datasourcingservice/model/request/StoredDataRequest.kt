package org.dataland.datasourcingservice.model.request

import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
import java.util.Date
import java.util.UUID

/**
 * DTO for creating and transferring request data.
 */
data class StoredDataRequest(
    val id: UUID,
    val companyId: UUID,
    val reportingPeriod: String,
    val dataType: String,
    val userId: UUID,
    val creationTimeStamp: Date,
    val memberComment: String? = null,
    val adminComment: String? = null,
    val lastModifiedDate: Date,
    val requestPriority: RequestPriority,
    val state: RequestState,
    val dataSourcingEntityId: UUID? = null,
) {
    companion object {
        /**
         * Converts a RequestEntity to a StoredDataRequest.
         *
         * @param entity The RequestEntity to convert.
         * @return The corresponding StoredDataRequest.
         */
        fun fromRequestEntity(entity: RequestEntity): StoredDataRequest =
            StoredDataRequest(
                id = entity.id,
                companyId = entity.companyId,
                reportingPeriod = entity.reportingPeriod,
                dataType = entity.dataType,
                userId = entity.userId,
                creationTimeStamp = entity.creationTimeStamp,
                memberComment = entity.memberComment,
                adminComment = entity.adminComment,
                lastModifiedDate = entity.lastModifiedDate,
                requestPriority = entity.requestPriority,
                state = entity.state,
                dataSourcingEntityId = entity.dataSourcingEntity?.id,
            )
    }

    /**
     * Converts this StoredDataRequest to a RequestEntity.
     *
     * @return The corresponding RequestEntity.
     */
    fun toRequestEntity(): RequestEntity =
        RequestEntity(
            id = this.id,
            companyId = this.companyId,
            reportingPeriod = this.reportingPeriod,
            dataType = this.dataType,
            userId = this.userId,
            creationTimeStamp = this.creationTimeStamp,
            memberComment = this.memberComment,
            adminComment = this.adminComment,
            lastModifiedDate = this.lastModifiedDate,
            requestPriority = this.requestPriority,
            state = this.state,
            dataSourcingEntity = null,
        )
}
