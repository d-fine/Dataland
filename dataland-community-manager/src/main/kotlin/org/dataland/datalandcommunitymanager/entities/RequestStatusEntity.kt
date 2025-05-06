package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.dataland.datalandcommunitymanager.converters.RequestStatusEnumAttributeConverter
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestStatusObject
import java.util.UUID

/**
 * The entity storing the information considering one data request status
 */
@Entity
@Table(name = "request_status_history")
data class RequestStatusEntity(
    @Id
    val statusHistoryId: String,
    @Convert(converter = RequestStatusEnumAttributeConverter::class)
    val requestStatus: RequestStatus,
    @Enumerated(EnumType.STRING)
    val accessStatus: AccessStatus,
    val creationTimestamp: Long,
    @ManyToOne(optional = false)
    @JoinColumn(name = "data_request_id")
    var dataRequest: DataRequestEntity,
    val requestStatusChangeReason: String? = null,
    val answeringDataId: String? = null,
) {
    constructor(
        statusObject: StoredDataRequestStatusObject,
        dataRequest: DataRequestEntity,
    ) : this(
        statusHistoryId = UUID.randomUUID().toString(),
        requestStatus = statusObject.status,
        accessStatus = statusObject.accessStatus,
        creationTimestamp = statusObject.creationTimestamp,
        requestStatusChangeReason = statusObject.requestStatusChangeReason,
        dataRequest = dataRequest,
        answeringDataId = statusObject.answeringDataId,
    )

    /**
     * Converts this entity to a message object
     * @returns the generated message object
     */
    fun toStoredDataRequestStatusObject() =
        StoredDataRequestStatusObject(
            status = requestStatus,
            accessStatus = accessStatus,
            creationTimestamp = creationTimestamp,
            requestStatusChangeReason = requestStatusChangeReason,
            answeringDataId = answeringDataId,
        )
}
