package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestStatusObject
import java.util.*

/**
 * The entity storing the information considering one data request status
 */
@Entity
@Table(name = "request_status_history")
data class RequestStatusEntity(
    @Id
    val statusHistoryId: String,

    @Enumerated(EnumType.STRING)
    val requestStatus: RequestStatus,

    val creationTimestamp: Long,

    @ManyToOne(optional = false)
    @JoinColumn(name = "data_request_id")
    var dataRequestEntity: DataRequestEntity,
) {
    constructor(
        statusObject: StoredDataRequestStatusObject,
        dataRequest: DataRequestEntity,
    ) : this(
        statusHistoryId = UUID.randomUUID().toString(),
        requestStatus = statusObject.status,
        creationTimestamp = statusObject.creationTimestamp,
        dataRequestEntity = dataRequest,
    )

    /**
     * Converts this entity to a message object
     * @returns the generated message object
     */
    fun toStoredDataRequestStatusObject() = StoredDataRequestStatusObject(
        status = requestStatus,
        creationTimestamp = creationTimestamp,
    )
}
