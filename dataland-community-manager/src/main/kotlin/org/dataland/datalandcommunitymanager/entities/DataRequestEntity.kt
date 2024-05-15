package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestMessageObject
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestStatusObject
import java.util.*

/**
 * The entity storing the information considering one data request
 */
@Entity
@Table(name = "data_requests")
data class DataRequestEntity(
    @Id
    @Column(name = "data_request_id")
    val dataRequestId: String,

    val userId: String,

    val creationTimestamp: Long,

    val dataType: String,

    val reportingPeriod: String,

    val datalandCompanyId: String,

    @OneToMany(mappedBy = "dataRequest")
    var messageHistory: List<MessageEntity>,

    // todo check if dataRequest works as well
    @OneToMany(mappedBy = "dataRequestEntity")
    var dataRequestStatusHistory: List<RequestStatusEntity>,

    var lastModifiedDate: Long,

) {
    val requestStatus: RequestStatus
        get() = (dataRequestStatusHistory.maxByOrNull { it.creationTimestamp }?.requestStatus) ?: RequestStatus.Open
    constructor(
        userId: String,
        dataType: String,
        reportingPeriod: String,
        datalandCompanyId: String,
        creationTimestamp: Long,
    ) : this(
        dataRequestId = UUID.randomUUID().toString(),
        userId = userId,
        creationTimestamp = creationTimestamp,
        dataType = dataType,
        reportingPeriod = reportingPeriod,
        datalandCompanyId = datalandCompanyId,
        messageHistory = listOf(),
        dataRequestStatusHistory = listOf(),
        lastModifiedDate = creationTimestamp,
    )

    /**
     * Associates a message history
     * This must be done after creation and storage of the DataRequestEntity
     * due to cross dependencies between entities
     * @param messageHistory a list of ordered message objects
     */
    fun associateMessages(messageHistory: List<StoredDataRequestMessageObject>) {
        this.messageHistory = messageHistory.map {
            MessageEntity(it, this)
        }
    }

    /**
     * Associates a request status history
     * This must be done after creation and storage of the DataRequestEntity
     * due to cross dependencies between entities
     * @param requestStatusHistory a list of ordered request status objects
     */
    fun associateRequestStatus(requestStatusHistory: List<StoredDataRequestStatusObject>) {
        this.dataRequestStatusHistory = requestStatusHistory.map {
            RequestStatusEntity(it, this)
        }
    }

    /**
     * Converts this entity to a StoredDataRequest
     * @returns the StoredDataRequest
     */
    fun toStoredDataRequest() = StoredDataRequest(
        dataRequestId = dataRequestId,
        userId = userId,
        creationTimestamp = creationTimestamp,
        dataType = dataType,
        reportingPeriod = reportingPeriod,
        datalandCompanyId = datalandCompanyId,
        messageHistory = messageHistory
            .sortedBy { it.creationTimestamp }
            .map { it.toStoredDataRequestMessageObject() },
        dataRequestStatusHistory = dataRequestStatusHistory
            .sortedBy { it.creationTimestamp }
            .map { it.toStoredDataRequestStatusObject() },
        lastModifiedDate = lastModifiedDate,
        requestStatus = requestStatus,
    )
}
