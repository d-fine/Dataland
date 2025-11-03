package org.dataland.datasourcingservice.model.request

import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
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
    val creationTimestamp: Long,
    val memberComment: String? = null,
    val adminComment: String? = null,
    val lastModifiedDate: Long,
    val requestPriority: RequestPriority,
    val state: RequestState,
    val dataSourcingEntityId: String? = null,
) {
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
            creationTimestamp = this.creationTimestamp,
            memberComment = this.memberComment,
            adminComment = this.adminComment,
            lastModifiedDate = this.lastModifiedDate,
            requestPriority = this.requestPriority,
            state = this.state,
            dataSourcingEntity = null,
        )
}
