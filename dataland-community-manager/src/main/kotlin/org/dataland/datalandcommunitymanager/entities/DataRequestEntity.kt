package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestMessageObject
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

    var lastModifiedDate: Long,

    @Enumerated(EnumType.STRING)
    var requestStatus: RequestStatus,
) {
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
        lastModifiedDate = creationTimestamp,
        requestStatus = RequestStatus.Open,
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
        lastModifiedDate = lastModifiedDate,
        requestStatus = requestStatus,
    )
}
